package com.notesvault.model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.notesvault.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class PasswordRecoveryService {
    private final Logger log = LoggerFactory.getLogger(PasswordRecoveryService.class);
    private final Firestore firestore;
    private final TokenGeneratorService tokenGeneratorService;
    private final EmailService emailService;

    public PasswordRecoveryService(Firestore firestore, TokenGeneratorService tokenGenerator, EmailService emailService) {
        this.firestore = firestore;
        this.tokenGeneratorService = tokenGenerator;
        this.emailService = emailService;
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
            TokenGeneratorService.GeneratedTokenInfo tokenInfo = tokenGeneratorService.generateSecureToken(email);
            log.info("Token generado y almacenado para {}. Token crudo inicia con: {}", email,
                    tokenInfo.getRawToken().substring(0, Math.min(tokenInfo.getRawToken().length(), 8)) + "...");

            // Obtener el nombre del usuario si está disponible
            String userName = document.getString("name");
            
            // Enviar correo de recuperación
            try {
                emailService.sendPasswordRecoveryEmail(email, tokenInfo.getRawToken(), userName);
                log.info("Correo de recuperación enviado exitosamente a: {}", email);
            } catch (Exception e) {
                log.error("Error al enviar correo de recuperación a {}: {}", email, e.getMessage());
                // No lanzamos excepción aquí para no revelar información sobre la existencia del usuario
                // En un entorno de producción, podrías querer manejar esto de manera diferente
            }

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
            // Buscar tokens activos para el email
            ApiFuture<QuerySnapshot> future = firestore.collection("activeTokens")
                .whereEqualTo("userEmail", email)
                .get();
            
            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            if (documents.isEmpty()) {
                log.warn("No se encontraron tokens activos para el email: {}", email);
                return false;
            }

            // Verificar cada token encontrado
            for (QueryDocumentSnapshot document : documents) {
                String storedHashedToken = document.getString("hashedToken");
                
                // Verificar que el token proporcionado coincida con el hash almacenado
                if (BCrypt.checkpw(token, storedHashedToken)) {
                    // Verificar que el token no haya expirado
                    Timestamp expirationTimestamp = document.getTimestamp("expirationTime");
                    if (expirationTimestamp == null) {
                        log.warn("Token sin timestamp de expiración para usuario: {}", email);
                        return false;
                    }
                    
                    Instant expirationTime = expirationTimestamp.toDate().toInstant();
                    if (Instant.now().isAfter(expirationTime)) {
                        log.warn("Token expirado para usuario: {}", email);
                        // Eliminar token expirado
                        firestore.collection("activeTokens").document(document.getId()).delete();
                        return false;
                    }

                    log.info("Token verificado exitosamente para usuario: {}", email);
                    return true;
                }
            }

            log.warn("Token no válido para el email: {}", email);
            return false;

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
        if (!verifyRecoveryToken(token, email)) {
            log.warn("Token inválido para cambio de contraseña: {}", email);
            return false;
        }

        try {
            // Hash de la nueva contraseña
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            
            // Actualizar contraseña en Firestore
            ApiFuture<com.google.cloud.firestore.WriteResult> future = 
                firestore.collection("users").document(email).update("password", hashedPassword);
            
            future.get(); // Esperar a que se complete la actualización

            // Eliminar todos los tokens activos para este usuario
            ApiFuture<QuerySnapshot> tokensFuture = firestore.collection("activeTokens")
                .whereEqualTo("userEmail", email)
                .get();
            
            QuerySnapshot tokensSnapshot = tokensFuture.get();
            for (QueryDocumentSnapshot document : tokensSnapshot.getDocuments()) {
                firestore.collection("activeTokens").document(document.getId()).delete();
            }

            log.info("Contraseña cambiada exitosamente para usuario: {}", email);
            return true;

        } catch (Exception e) {
            log.error("Error al cambiar contraseña para usuario {}: {}", email, e.getMessage());
            return false;
        }
    }
}
