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
        // Basic empty validation, this should be considered in the frontend too
        if(email == null || email.isEmpty()){
            log.warn("Recovery attempt with empty email");
            throw new UserNotFoundException("Email is required");
        }

        try {
            // Here we verify if the email is within our users
            ApiFuture<DocumentSnapshot> future = firestore.collection("users").document(email).get();
            DocumentSnapshot document = future.get();

            if(!document.exists()){
                log.warn("Email not found during password recovery {}", email);
                throw new UserNotFoundException("There is an error, please verify the credential");
            }

            // Also verify that the user is active in the system
            Boolean isActive = document.getBoolean("isActive");
            if (isActive == null || !isActive) {
                log.warn("Recovery attempt on deactivated account: {}", email);
                throw new UserNotFoundException("The account is deactivated");
            }

            log.info("Generating recovery token for: {}", email);

            // Generate unique token
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(email, "recovery");
            log.info("Token generated and stored for {}. Raw token starts with: {}", email,
                    tokenInfo.getRawToken().substring(0, Math.min(tokenInfo.getRawToken().length(), 8)) + "...");

            // Get the username if available
            String userName = document.getString("userName");
            
            // Send recovery email asynchronously (does not block response)
            emailService.sendPasswordRecoveryEmailAsync(email, tokenInfo.getRawToken(), userName)
                .exceptionally(throwable -> {
                    log.error("Error sending recovery email to {}: {}", email, throwable.getMessage());
                    return null;
                });

        }catch (InterruptedException e) {
            log.error("Error verifying user in Firestore: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new UserNotFoundException("Error verifying user");
        } catch (ExecutionException e) {
            log.error("Execution error verifying user: {}", e.getMessage());
            throw new UserNotFoundException("Error verifying user");
        }
    }

    /**
     * Verifies if a recovery token is valid
     * @param token Recovery token
     * @param email User email
     * @return true if valid, false otherwise
     */
    public boolean verifyRecoveryToken(String token, String email) {
        if (token == null || token.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            log.warn("Empty token or email in verification");
            return false;
        }

        try {
            return tokenService.verifyToken(token, email, "recovery");
        } catch (Exception e) {
            log.error("Error verifying token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Changes the user's password using a valid recovery token
     * @param token Recovery token
     * @param email User email
     * @param newPassword New password
     * @return true if changed successfully, false otherwise
     */
    public boolean changePasswordWithToken(String token, String email, String newPassword) {
        log.info("Starting password change for user: {}", email);
        
        // Verify and consume the token (automatically deletes if valid)
        if (!tokenService.verifyAndConsumeToken(token, email, "recovery")) {
            log.warn("Invalid or already consumed token for password change: {}", email);
            return false;
        }

        try {
            // Hash the new password
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            
            // Update password in Firestore
            ApiFuture<com.google.cloud.firestore.WriteResult> future = 
                firestore.collection("users").document(email).update("password", hashedPassword);
            
            future.get(); // Wait for the update to complete

            // Asynchronously delete any additional tokens for this user
            CompletableFuture.runAsync(() -> {
                try {
                    boolean deleted = tokenService.deleteAllTokensForUser(email, "recovery");
                    if (deleted) {
                        log.info("Additional tokens deleted for user: {}", email);
                    } else {
                        log.debug("No additional tokens found to delete for user: {}", email);
                    }
                } catch (Exception e) {
                    log.error("Error deleting additional tokens for user {}: {}", email, e.getMessage());
                }
            }, cleanupTaskExecutor);

            log.info("Password successfully changed for user: {}", email);
            return true;

        } catch (Exception e) {
            log.error("Error changing password for user {}: {}", email, e.getMessage());
            return false;
        }
    }
}
