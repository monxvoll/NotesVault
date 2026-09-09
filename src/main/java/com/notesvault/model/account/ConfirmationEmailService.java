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
     * Asynchronously sends a confirmation email
     * @param uid User's uid
     * @param email Recipient's email
     * @param token Generated confirmation token
     * @param userName User's name
     * @return CompletableFuture that completes when the email is sent
     */
    public CompletableFuture<Void> sendConfirmationEmailAsync(String uid, String email, String token, String userName) {
        try {
            String content = buildConfirmationEmailContent(uid, token, userName);
            return emailService.sendEmailAsync(email, "Account Confirmation - NotesVault", content)
                .thenRun(() -> logger.info("Confirmation email sent successfully to: {}", uid))
                .exceptionally(throwable -> {
                    logger.error("Error sending confirmation email to {}: {}", uid, throwable.getMessage());
                    return null;
                });
        } catch (Exception e) {
            logger.error("Error preparing confirmation email for {}: {}", uid, e.getMessage());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public String buildConfirmationEmailContent(String uid, String token, String userName){
        String greeting = userName != null ? "Hello " + userName : "Hello";
        String confirmationUrl = baseUrl + "confirm?token=" + token + "&uid=" + uid;
        return String.format("""
            %s,

            You have requested to confirm your NotesVault account.

            To continue the confirmation process, click on the following link:
            %s
                """, greeting, confirmationUrl);
    }

    public boolean confirmAccount(String token, String uid){
        logger.info("Starting account confirmation for user: {}", uid);
        
        try{
            // Verify and consume the token (automatically deletes if valid)
            boolean isTokenValid = tokenService.verifyAndConsumeToken(token, uid, "confirmation");
            
            if(isTokenValid){
                logger.info("Token valid and consumed for user: {}", uid);
                
                // Update the user's isConfirmed state in Firestore
                boolean updateSuccess = updateUserConfirmationStatus(uid);
                if (updateSuccess) {
                    logger.info("Account successfully confirmed for user: {}", uid);
                    
                    // Asynchronously delete any additional confirmation tokens
                    CompletableFuture.runAsync(() -> {
                        try {
                            boolean deleted = tokenService.deleteAllTokensForUser(uid, "confirmation");
                            if (deleted) {
                                logger.info("Additional confirmation tokens deleted for user: {}", uid);
                            } else {
                                logger.debug("No additional confirmation tokens found to delete for user: {}", uid);
                            }
                        } catch (Exception e) {
                            logger.error("Error deleting additional confirmation tokens for user {}: {}", uid, e.getMessage());
                        }
                    }, cleanupTaskExecutor);
                    
                    return true;
                } else {
                    logger.error("Error updating confirmation state for user: {}", uid);
                    return false;
                }
            } else {
                logger.warn("Invalid or already consumed token for account confirmation: {}", uid);
                return false;
            }
        }catch(Exception e){
            logger.error("Error confirming account for user {}: {}", uid, e.getMessage());
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
            logger.info("Confirmation state updated successfully for user {} at: {}", 
                       uid, result.getUpdateTime());
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error updating confirmation state in Firestore for user {}: {}", 
                        uid, e.getMessage(), e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Verifies if a user is already confirmed
     * @param uid user's uid
     * @return true if the user is confirmed, false otherwise
     */
    public boolean isUserConfirmed(String uid) {
        try {
            ApiFuture<com.google.cloud.firestore.DocumentSnapshot> future = 
                firestore.collection("users").document(uid).get();
            
            com.google.cloud.firestore.DocumentSnapshot document = future.get();
            if (!document.exists()) {
                logger.warn("User not found: {}", uid);
                return false;
            }
            
            Boolean isConfirmed = document.getBoolean("confirmed");
            return isConfirmed != null && isConfirmed;
            
        } catch (Exception e) {
            logger.error("Error verifying confirmation state for user {}: {}", uid, e.getMessage());
            return false;
        }
    }
}
