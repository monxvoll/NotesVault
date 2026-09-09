package com.notesvault.model.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.notesvault.model.contracts.EmailService;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailRecoveryService {
    private static final Logger logger = LoggerFactory.getLogger(EmailRecoveryService.class);
    
    private final EmailService emailService;

    @Value("${app.recovery.base-url}")
    private String baseUrl;
    
    public EmailRecoveryService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Asynchronously sends a password recovery email
     * @param toEmail Recipient's email
     * @param recoveryToken Generated recovery token
     * @param userName User's name
     * @return CompletableFuture that completes when the email is sent
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendPasswordRecoveryEmailAsync(String toEmail, String recoveryToken, String userName) {
        try {
            String content = buildRecoveryEmailContent(toEmail, recoveryToken, userName);
            return emailService.sendEmailAsync(toEmail, "Password Recovery - NotesVault", content)
                .thenRun(() -> logger.info("Recovery email sent successfully to: {}", toEmail))
                .exceptionally(throwable -> {
                    logger.error("Error sending recovery email to {}: {}", toEmail, throwable.getMessage());
                    return null;
                });
        } catch (Exception e) {
            logger.error("Error preparing recovery email for {}: {}", toEmail, e.getMessage());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Builds the content of the recovery email
     * @param email User email
     * @param token Recovery token
     * @param userName User's name (optional)
     * @return Email content
     */
    private String buildRecoveryEmailContent(String email, String token, String userName) {
        String greeting = userName != null ? "Hello " + userName : "Hello";
        String recoveryUrl = baseUrl + "/recovery/reset?token=" + token + "&email=" + email;
        
        return String.format("""
            %s,
            
            You have requested to recover your NotesVault password.
            
            To continue with the recovery process, click the following link:
            %s
            
            This link is valid for 24 hours. If you did not request this change, you can ignore this email.
            
            If the link does not work, copy and paste the following URL into your browser:
            %s
            
            Sincerely,
            The NotesVault Team
            
            ---
            This is an automated email, please do not reply to this message.
            """, greeting, recoveryUrl, recoveryUrl);
    }

}
