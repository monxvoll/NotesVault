package com.notesvault.model.account;


import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Service
public class DeletionService {
    private static final Logger logger = LoggerFactory.getLogger(DeletionService.class);
    private final FirebaseAuth firebaseAuth;
    private final TokenService tokenService;
    private final DeletionEmailService accountDeletionEmailService;
    private final Firestore firestore;
    private final Executor cleanupTaskExecutor;

    public DeletionService(FirebaseAuth firebaseAuth, TokenService tokenService, DeletionEmailService accountDeletionEmailService, Firestore firestore, Executor cleanupTaskExecutor) {
        this.firebaseAuth = firebaseAuth;
        this.tokenService = tokenService;
        this.accountDeletionEmailService = accountDeletionEmailService;
        this.firestore = firestore;
        this.cleanupTaskExecutor = cleanupTaskExecutor;
    }

    public void initiateAccountDeletion(String uid) {
        logger.info("Request to initiate account deletion for: {}" + uid);

        try {
            UserRecord  userRecord = firebaseAuth.getUser(uid);
            String email = userRecord.getEmail();

            if(email==null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The user does not have an associated email.");
            }

            //Send token
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(uid, "confirmation");
            logger.info("Deletion confirmation token generated for: {}", email);

            accountDeletionEmailService.sendAccountDeletionAsync(email, tokenInfo.getRawToken(), null, uid)
                    .exceptionally(throwable -> {
                        logger.error("Error sending deletion email to {}: {}", email, throwable.getMessage());
                        return null;
                    });

            logger.info("Deletion confirmation email sent successfully to: {}", email);


        } catch (FirebaseAuthException e) {
            logger.error("Firebase error finding user {}: {}", uid, e.getAuthErrorCode());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.", e);
        }
    }

    public boolean confirmAccountDeletion(String token, String uid) {
        logger.info("Confirming account deletion for: {}", uid);

        if (!tokenService.verifyToken(token, uid, "confirmation")) {
            logger.warn("Invalid or already consumed deletion token for: {}", uid);
            return false;
        }
        try {

            // Disable account on firebase auth
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setDisabled(true);
            firebaseAuth.updateUser(request);
            logger.info("User account {} disabled in Firebase Authentication.", uid);

            // Set account as disable on firestore
            firestore.collection("users").document(uid).update("active", false, "deletedAt", FieldValue.serverTimestamp()).get();
            logger.info("User profile {} marked as inactive in Firestore.", uid);

            // Clean tokens
            CompletableFuture.runAsync(() -> tokenService.deleteAllTokensForUser(uid, "confirmation"), cleanupTaskExecutor);

            return true;
        } catch (Exception e) {
            logger.error("Error processing account deletion for {}: {}", uid, e.getMessage());
            return false;
        }
    }


    public boolean isAccountDeleted(String uid){
        try{

            UserRecord userRecord = firebaseAuth.getUser(uid);

            // We checked both on auth both firestore
            DocumentSnapshot document= firestore.collection("users").document(uid).get().get();
            Boolean isActiveInFirestore = document.exists() ? document.getBoolean("active") : Boolean.FALSE;

            return userRecord.isDisabled() || Boolean.FALSE.equals(isActiveInFirestore);

        }catch (FirebaseAuthException e) {
            logger.error("Firebase error verifying deletion state for {}: {}", uid, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("General error verifying deletion state for {}: {}", uid, e.getMessage());
            return false;
        }
    }

}
