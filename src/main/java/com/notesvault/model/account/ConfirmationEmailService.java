package com.notesvault.model.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import com.notesvault.model.contracts.EmailService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;

@Service
public class ConfirmationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmationEmailService.class);
    @Value("${app.confirmation.base-url}")
    private String baseUrl;

    private final EmailService emailService;
    private final TokenService tokenService;
    private final Firestore firestore;
    private final Executor cleanupTaskExecutor;

    public ConfirmationEmailService(EmailService emailService, TokenService tokenService, Firestore firestore,
                                    @org.springframework.beans.factory.annotation.Qualifier("cleanupTaskExecutor") Executor cleanupTaskExecutor) {
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.firestore = firestore;
        this.cleanupTaskExecutor = cleanupTaskExecutor;
    }

    /**
     * Envía un correo de confirmación de forma asíncrona
     * @param email Email del destinatario
     * @param token Token de confirmación generado
     * @param userName Nombre del usuario
     * @return CompletableFuture que se completa cuando se envía el email
     */
    public CompletableFuture<Void> sendConfirmationEmailAsync(String uid, String email, String token, String userName) {
        try {
            String content = buildConfirmationEmailContent(uid, token, userName);
            return emailService.sendEmailAsync(email, "Confirmación de Cuenta - NotesVault", content)
                .thenRun(() -> logger.info("Correo de confirmación enviado exitosamente a: {}", uid))
                .exceptionally(throwable -> {
                    logger.error("Error al enviar correo de confirmación a {}: {}", uid, throwable.getMessage());
                    return null;
                });
        } catch (Exception e) {
            logger.error("Error al preparar correo de confirmación para {}: {}", uid, e.getMessage());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public String buildConfirmationEmailContent(String uid, String token, String userName){
        String greeting = userName != null ? "Hola " + userName : "Hola";
        String confirmationUrl = baseUrl + "confirm?token=" + token + "&uid=" + uid;
        return String.format("""
            %s,

            Has solicitado confirmar tu cuenta en NotesVault.

            Para continuar con el proceso de confirmación, haz clic en el siguiente enlace:
            %s
                """, greeting, confirmationUrl);
    }

    public boolean confirmAccount(String token, String uid){
        logger.info("Iniciando confirmación de cuenta para usuario: {}", uid);
        
        try{
            // Verificar y consumir el token (lo elimina automáticamente si es válido)
            boolean isTokenValid = tokenService.verifyAndConsumeToken(token, uid, "confirmation");
            
            if(isTokenValid){
                logger.info("Token válido y consumido para usuario: {}", uid);
                
                // Actualizar el estado isConfirmed del usuario en Firestore
                boolean updateSuccess = updateUserConfirmationStatus(uid);
                if (updateSuccess) {
                    logger.info("Cuenta confirmada exitosamente para el usuario: {}", uid);
                    
                    // Eliminar cualquier token adicional de confirmación de forma asíncrona
                    CompletableFuture.runAsync(() -> {
                        try {
                            boolean deleted = tokenService.deleteAllTokensForUser(uid, "confirmation");
                            if (deleted) {
                                logger.info("Tokens adicionales de confirmación eliminados para usuario: {}", uid);
                            } else {
                                logger.debug("No se encontraron tokens adicionales de confirmación para eliminar para usuario: {}", uid);
                            }
                        } catch (Exception e) {
                            logger.error("Error al eliminar tokens adicionales de confirmación para usuario {}: {}", uid, e.getMessage());
                        }
                    }, cleanupTaskExecutor);
                    
                    return true;
                } else {
                    logger.error("Error al actualizar el estado de confirmación para el usuario: {}", uid);
                    return false;
                }
            } else {
                logger.warn("Token inválido o ya consumido para confirmación de cuenta: {}", uid);
                return false;
            }
        }catch(Exception e){
            logger.error("Error al confirmar la cuenta para el usuario {}: {}", uid, e.getMessage());
            return false;
        }
    }

    private boolean updateUserConfirmationStatus(String uid) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("confirmed", true);

            ApiFuture<WriteResult> future = firestore.collection("users")
                    .document(uid)
                    .update(updates);

            WriteResult result = future.get();
            logger.info("Estado de confirmación actualizado exitosamente para el usuario {} en: {}", 
                       uid, result.getUpdateTime());
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error al actualizar el estado de confirmación en Firestore para el usuario {}: {}", 
                        uid, e.getMessage(), e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Verifica si un usuario ya está confirmado
     * @param uid uid del usuario
     * @return true si el usuario está confirmado, false en caso contrario
     */
    public boolean isUserConfirmed(String uid) {
        try {
            ApiFuture<com.google.cloud.firestore.DocumentSnapshot> future = 
                firestore.collection("users").document(uid).get();
            
            com.google.cloud.firestore.DocumentSnapshot document = future.get();
            if (!document.exists()) {
                logger.warn("Usuario no encontrado: {}", uid);
                return false;
            }
            
            Boolean isConfirmed = document.getBoolean("confirmed");
            return isConfirmed != null && isConfirmed;
            
        } catch (Exception e) {
            logger.error("Error al verificar estado de confirmación para usuario {}: {}", uid, e.getMessage());
            return false;
        }
    }
}
