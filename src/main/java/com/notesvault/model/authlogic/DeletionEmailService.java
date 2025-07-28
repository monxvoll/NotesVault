package com.notesvault.model.authlogic;


import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.notesvault.model.contracts.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


@Service
public class DeletionEmailService {

    private static final Logger logger = LoggerFactory.getLogger(DeletionEmailService.class);
    @Value("${app.elimination.base-url}")
    private String baseUrl;

    private final EmailService emailService;
    private final TokenService tokenService;
    private final Firestore firestore;
    private final Executor cleanupTaskExecutor;

    public DeletionEmailService(EmailService emailService, TokenService tokenService, Firestore firestore, Executor cleanupTaskExecutor) {
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.firestore = firestore;
        this.cleanupTaskExecutor = cleanupTaskExecutor;
    }

    /**
     * Enviamos un correo de confimación de eliminacion (cuenta) de forma asincrona)
     *
     * @param email    Email del destinatario
     * @param token    Token de confirmación generado
     * @param userName Nombre del usuario
     * @return CompletableFuture que se completa cuando se envía el email
     */

    public CompletableFuture<Void> sendAccountDeletionAsync(String email, String token, String userName) {
        try {
            String content = buildAccountDeletionEmailContent(email, token, userName);
            return emailService.sendEmailAsync(email, "Confirmación de eliminación de cuenta – NotesVault", content)
                    .thenRun(() -> logger.info("Correo de eliminación de cuenta enviado exitosamente a: {}", email))
                    .exceptionally(throwable -> {
                        logger.error("Error al enviar correo de  eliminación de cuenta a {}: {}", email, throwable.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Error al preparar el correo de eliminacion de cuenta para {}: {}", email, e.getMessage());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }


    private String buildAccountDeletionEmailContent(String email, String token, String userName) {
        String greeting = userName != null ? "Hola " + userName : "Hola";
        String confirmationUrl = baseUrl + "delete-confirmation?token=" + token + "&email=" + email;
        return String.format("""
                %s,

                Has solicitado eliminar tu cuenta en NotesVault.

                Para continuar con el proceso de eliminación, haz clic en el siguiente enlace:
                %s
                    """, greeting, confirmationUrl);
    }

    public boolean deleteAccount(String token, String email) {
        try {
            logger.info("Iniciando eliminación de cuenta para usuario: {}", email);

            //Verificamos y consumimos el token( en caso de ser valido se borra automaticament)
            boolean validToken = tokenService.verifyAndConsumeToken(token, email, "confirmation");

            if (validToken) {
                logger.info("Token válido y consumido para usuario: {}", email);
                //Actualizamos el estado isActive en firestore y guardamos timestamp
                boolean updateSuccess = updateAccountDeletionStatus(email);
                if (updateSuccess) {
                    logger.info("Cuenta eliminadada exitosamente para el usuario: {}", email);
                    // Eliminar cualquier token adicional de confirmación de forma asíncrona
                    CompletableFuture.runAsync(() -> {
                        try {
                            boolean deleted = tokenService.deleteAllTokensForUser(email, "confirmation");
                            if (deleted) {
                                logger.info("Tokens adicionales de eliminación eliminados para la cuenta: {}", email);
                            } else {
                                logger.debug("No se encontraron tokens adicionales de eliminación para la cuenta: {}", email);
                            }
                        } catch (Exception e) {
                            logger.error("Error al eliminar tokens adicionales de eliminación para la cuenta {}: {}", email, e.getMessage());
                        }
                    }, cleanupTaskExecutor);

                    return true;
                } else {
                    logger.error("Error al actualizar el estado de eliminación de la cuenta: {}", email);
                    return false;
                }
            } else {
                logger.warn("Token inválido o ya consumido para eliminacíon de cuenta: {}", email);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error al eliminar la cuenta para el usuario {}: {}", email, e.getMessage());
            return false;
        }
    }

    private boolean updateAccountDeletionStatus(String email) {
        try {
            DocumentReference document = firestore.collection("users").document(email);

            // Se marca la cuenta como inactiva y se guarda el timestamp de eliminación
            document.update("isActive", false, "deletedAt", FieldValue.serverTimestamp()).get();
            logger.info("Cuenta del usuario {} marcada como inactiva en Firestore", email);
            return true;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al actualizar el estado de eliminación en Firestore para la cuenta {}: {}",
                    email, e.getMessage(), e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean isAccountDeleted(String email){
        try {
            DocumentReference document = firestore.collection("users").document(email);
            DocumentSnapshot snapshot = document.get().get();

            if (!snapshot.exists()) {
                logger.warn("Usuario {} no encontrado en Firestore", email);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }

            Boolean isActive = snapshot.getBoolean("isActive");
            return isActive != null && !isActive;

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al verificar el estado de eliminación en Firestore para la cuenta {}: {}",
                    email, e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al verificar la cuenta", e);
        }
    }
}