package com.notesvault.model.account;

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
     * Asynchronously sends an account deletion confirmation email
     *
     * @param email    Recipient's email
     * @param token    Generated confirmation token
     * @param userName User's name
     * @return CompletableFuture that completes when the email is sent
     */

    public CompletableFuture<Void> sendAccountDeletionAsync(String email, String token, String userName, String uid) {
        try {
            String content = buildAccountDeletionEmailContent(email, token, userName, uid);
            return emailService.sendEmailAsync(email, "Account Deletion Confirmation – NotesVault", content)
                    .thenRun(() -> logger.info("Account deletion email sent successfully to: {}", email))
                    .exceptionally(throwable -> {
                        logger.error("Error sending account deletion email to {}: {}", email, throwable.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Error preparing account deletion email for {}: {}", email, e.getMessage());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    private String buildAccountDeletionEmailContent(String email, String token, String userName, String uid) {
        String greeting = userName != null ? "Hello " + userName : "Hello";
        String confirmationUrl = baseUrl + "delete-confirmation?token=" + token + "&uid=" + uid;
        return String.format("""
                %s,

                You have requested to delete your NotesVault account.

                To continue with the deletion process, click the following link:
                %s
                    """, greeting, confirmationUrl);
    }
}

