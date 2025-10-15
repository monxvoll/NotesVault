package com.notesvault.model.authlogic;

import com.notesvault.model.contracts.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class DeletionEmailService {

    private static final Logger logger = LoggerFactory.getLogger(DeletionEmailService.class);
    @Value("${app.elimination.base-url}")
    private String baseUrl;
    private final EmailService emailService;


    public DeletionEmailService(EmailService emailService) {
        this.emailService = emailService;
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
}

