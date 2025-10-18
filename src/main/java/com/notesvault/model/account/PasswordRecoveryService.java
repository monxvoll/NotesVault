package com.notesvault.model.account;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.notesvault.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@Service
public class PasswordRecoveryService {
    private final Logger log = LoggerFactory.getLogger(PasswordRecoveryService.class);
    private final Firestore firestore;
    private final EmailRecoveryService emailService;
    private final Executor cleanupTaskExecutor;
    private final TokenService tokenService;

    public PasswordRecoveryService(Firestore firestore, EmailRecoveryService emailService, 
                                 @org.springframework.beans.factory.annotation.Qualifier("cleanupTaskExecutor") Executor cleanupTaskExecutor, 
                                 TokenService tokenService) {
        this.firestore = firestore;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.cleanupTaskExecutor = cleanupTaskExecutor;
    }

    public void generateRecoveryToken (String email) throws UserNotFoundException {
        //Validacion basica de vacio, esto se debe tomar en cuenta en el front tambien
        if(email == null || email.isEmpty()){
            log.warn("Intento de recuperacion con email vacio");
            throw new UserNotFoundException("El email es obligatorio");
        }

        try {
            //Justo aca verificamos si el email esta dentro de nuestros usuarios
            ApiFuture<DocumentSnapshot> future = firestore.collection("users").document(email).get();
            DocumentSnapshot document = future.get();

            if(!document.exists()){
                log.warn("Email no encontrado en la recuperacion de contrasena {}", email);
                throw new UserNotFoundException("Tenemos un error, por favor verifica la credencial");
            }

            //Verifico tambien que el usuario este activo en el sistema
            Boolean isActive = document.getBoolean("isActive");
            if (isActive == null || !isActive) {
                log.warn("Intento de recuperación en cuenta desactivada: {}", email);
                throw new UserNotFoundException("La cuenta se encuentra desactivada");
            }

            log.info("Generando token de recuperación para: {}", email);

            // Generar token único
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(email, "recovery");
            log.info("Token generado y almacenado para {}. Token crudo inicia con: {}", email,
                    tokenInfo.getRawToken().substring(0, Math.min(tokenInfo.getRawToken().length(), 8)) + "...");

            // Obtener el nombre del usuario si está disponible
            String userName = document.getString("userName");
            
            // Enviar correo de recuperación de forma asíncrona (no bloquea la respuesta)
            emailService.sendPasswordRecoveryEmailAsync(email, tokenInfo.getRawToken(), userName)
                .exceptionally(throwable -> {
                    log.error("Error al enviar correo de recuperación a {}: {}", email, throwable.getMessage());
                    return null;
                });

        }catch (InterruptedException e) {
            log.error("Error al verificar usuario en Firestore: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new UserNotFoundException("Error al verificar el usuario");
        } catch (ExecutionException e) {
            log.error("Error en la ejecución al verificar el usuario: {}", e.getMessage());
            throw new UserNotFoundException("Error al verificar el usuario");
        }
    }

    /**
     * Verifica si un token de recuperación es válido
     * @param token Token de recuperación
     * @param email Email del usuario
     * @return true si el token es válido, false en caso contrario
     */
    public boolean verifyRecoveryToken(String token, String email) {
        if (token == null || token.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            log.warn("Token o email vacío en verificación");
            return false;
        }

        try {
            return tokenService.verifyToken(token, email, "recovery");
        } catch (Exception e) {
            log.error("Error al verificar token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Cambia la contraseña del usuario usando un token de recuperación válido
     * @param token Token de recuperación
     * @param email Email del usuario
     * @param newPassword Nueva contraseña
     * @return true si se cambió exitosamente, false en caso contrario
     */
    public boolean changePasswordWithToken(String token, String email, String newPassword) {
        log.info("Iniciando cambio de contraseña para usuario: {}", email);
        
        // Verificar y consumir el token (lo elimina automáticamente si es válido)
        if (!tokenService.verifyAndConsumeToken(token, email, "recovery")) {
            log.warn("Token inválido o ya consumido para cambio de contraseña: {}", email);
            return false;
        }

        try {
            // Hash de la nueva contraseña
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            
            // Actualizar contraseña en Firestore
            ApiFuture<com.google.cloud.firestore.WriteResult> future = 
                firestore.collection("users").document(email).update("password", hashedPassword);
            
            future.get(); // Esperar a que se complete la actualización

            // Eliminar cualquier token adicional que pueda existir para este usuario de forma asíncrona
            CompletableFuture.runAsync(() -> {
                try {
                    boolean deleted = tokenService.deleteAllTokensForUser(email, "recovery");
                    if (deleted) {
                        log.info("Tokens adicionales eliminados para usuario: {}", email);
                    } else {
                        log.debug("No se encontraron tokens adicionales para eliminar para usuario: {}", email);
                    }
                } catch (Exception e) {
                    log.error("Error al eliminar tokens adicionales para usuario {}: {}", email, e.getMessage());
                }
            }, cleanupTaskExecutor);

            log.info("Contraseña cambiada exitosamente para usuario: {}", email);
            return true;

        } catch (Exception e) {
            log.error("Error al cambiar contraseña para usuario {}: {}", email, e.getMessage());
            return false;
        }
    }
}
