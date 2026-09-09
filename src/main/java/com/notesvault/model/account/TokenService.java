package com.notesvault.model.account;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final long  TOKEN_VALIDITY_HOURS = 24;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    private final Firestore firestore;

    public TokenService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Generates a secure token for the user
     * @param uid user's uid
     * @param type Token type (confirmation, recovery)
     * @return Generated token info
     */
    public GeneratedTokenInfo generateSecureToken(String uid, String type) {
        if (uid == null || uid.trim().isEmpty()) {
            logger.warn("Attempt to generate token without uid");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User uid is required to generate the token");
        }

        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        String rawToken = base64Encoder.encodeToString(randomBytes);

        Instant expirationTime = Instant.now().plus(TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS);

        String hashedToken = BCrypt.hashpw(rawToken, BCrypt.gensalt());
        
        // Generate a unique ID for the document
        String documentId = UUID.randomUUID().toString();

        Map<String, Object> tokenData = new HashMap<>();

        tokenData.put("uid", uid);
        tokenData.put("expirationTime", Timestamp.of(java.util.Date.from(expirationTime)));
        tokenData.put("hashedToken", hashedToken);
        tokenData.put("type", type);

        logger.info("Storing hashed token in Firestore for user: {}", uid);

        ApiFuture<WriteResult> writeResultFuture = firestore.collection("activeTokens").document(documentId).set(tokenData);

        try {
            WriteResult writeResult = writeResultFuture.get();
            logger.info("Hashed token successfully stored for user {} (UpdateTime: {})", uid, writeResult.getUpdateTime());
        } catch (InterruptedException e) {
            logger.error("Interruption while writing token in Firestore for {}: {}", uid, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error storing the token", e);
        } catch (ExecutionException e) {
            logger.error("Execution error while writing token in Firestore for {}: {}", uid, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error storing the token", e);
        }

        return new GeneratedTokenInfo(rawToken, hashedToken, expirationTime, documentId);
    }

    /**
     * Verifies if a token is valid for a user and specific type
     * @param token Token to verify
     * @param uid User's uid
     * @param type Token type (confirmation, recovery)
     * @return true if valid, false otherwise
     */
    public boolean verifyToken(String token, String uid, String type){
        try{
            ApiFuture<QuerySnapshot> future = firestore.collection("activeTokens")
            .whereEqualTo("uid", uid)
            .whereEqualTo("type", type)
            .limit(1)
            .get();

            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            if (documents.isEmpty()) {
                logger.warn("No active tokens found for uid: {}", uid);
                return false;
            }

            QueryDocumentSnapshot document = documents.get(0);
            String hashedToken = document.getString("hashedToken");

            if (BCrypt.checkpw(token, hashedToken)) {
                Timestamp expirationTimestamp = document.getTimestamp("expirationTime");
                if (expirationTimestamp == null) {
                    logger.warn("Token without expiration timestamp for user: {}",uid);
                    return false;
                }
                Instant expirationTime = expirationTimestamp.toDate().toInstant();
                if (Instant.now().isAfter(expirationTime)) {
                    logger.warn("Expired token for user: {}", uid);
                    firestore.collection("activeTokens").document(document.getId()).delete();
                    return false;
                }

                logger.info("Token successfully verified for user: {}", uid);
                return true;
            }

            logger.warn("Invalid token for email: {}", uid);
            return false;
        } catch (Exception e) {
            logger.error("Error verifying token", e);
            return false;
        }
    }

    /**
     * Verifies and consumes a token (deletes it after verification)
     * @param token Token to verify
     * @param uid user's uid
     * @param type Token type (confirmation, recovery)
     * @return true if token is valid and consumed, false otherwise
     */
    public boolean  verifyAndConsumeToken(String token, String uid, String type){
        try{
            ApiFuture<QuerySnapshot> future = firestore.collection("activeTokens")
            .whereEqualTo("uid", uid)
            .whereEqualTo("type", type)
            .limit(1)
            .get();

            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            if (documents.isEmpty()) {
                logger.warn("No active tokens found for user: {}", uid);
                return false;
            }

            QueryDocumentSnapshot document = documents.get(0);
            String hashedToken = document.getString("hashedToken");

            if (BCrypt.checkpw(token, hashedToken)) {
                Timestamp expirationTimestamp = document.getTimestamp("expirationTime");
                if (expirationTimestamp == null) {
                    logger.warn("Token without expiration timestamp for user: {}", uid);
                    return false;
                }
                Instant expirationTime = expirationTimestamp.toDate().toInstant();
                if (Instant.now().isAfter(expirationTime)) {
                    logger.warn("Expired token for user: {}", uid);
                    firestore.collection("activeTokens").document(document.getId()).delete();
                    return false;
                }

                // Valid token - delete it after verification
                firestore.collection("activeTokens").document(document.getId()).delete();
                logger.info("Token successfully verified and consumed for user: {}", uid);
                return true;
            }

            logger.warn("Invalid token for user: {}", uid);
            return false;
        } catch (Exception e) {
            logger.error("Error verifying and consuming token", e);
            return false;
        }
    }

    /**
     * Deletes all active tokens for a specific user
     * @param uid user's uid
     * @param type Token type (confirmation, recovery)
     * @return true if tokens were deleted, false otherwise
     */
    public boolean deleteAllTokensForUser(String uid, String type) {
        try {
            ApiFuture<QuerySnapshot> tokensFuture = firestore.collection("activeTokens")
                .whereEqualTo("userUid", uid)
                .whereEqualTo("type", type)
                .get();
            
            QuerySnapshot tokensSnapshot = tokensFuture.get();
            int deletedCount = 0;
            
            for (QueryDocumentSnapshot document : tokensSnapshot.getDocuments()) {
                firestore.collection("activeTokens").document(document.getId()).delete();
                deletedCount++;
            }
            
            logger.info("Deleted {} tokens for user: {} (type: {})", deletedCount, uid, type);
            return deletedCount > 0;
            
        } catch (Exception e) {
            logger.error("Error deleting tokens for user {}: {}", uid, e.getMessage());
            return false;
        }
    }

    public static class GeneratedTokenInfo {
        private final String rawToken;
        private final String hashedToken;
        private final Instant expirationTime;
        private final String documentId;

        public GeneratedTokenInfo(String rawToken, String hashedToken, Instant expirationTime, String documentId) {
            this.rawToken = rawToken;
            this.hashedToken = hashedToken;
            this.expirationTime = expirationTime;
            this.documentId = documentId;
        }

        public String getRawToken() {
            return rawToken;
        }

        public String getHashedToken() {
            return hashedToken;
        }

        public Instant getExpirationTime() {
            return expirationTime;
        }

        public String getDocumentId() {
            return documentId;
        }
    }

    @Scheduled(fixedRate = 60000)
    public void cleanExpiredTokens() {
        try {
            ApiFuture<QuerySnapshot> tokensFuture = firestore.collection("activeTokens")
                .whereLessThan("expirationTime", Timestamp.of(java.util.Date.from(Instant.now())))
                .get();
            
            QuerySnapshot tokensSnapshot = tokensFuture.get();
            int deletedCount = 0;
            
            for (QueryDocumentSnapshot document : tokensSnapshot.getDocuments()) {
                firestore.collection("activeTokens").document(document.getId()).delete();
                deletedCount++;
            }
            
            logger.info("Deleted {} expired tokens", deletedCount);
        } catch (Exception e) {
            logger.error("Error cleaning expired tokens", e);
        }
    }
}
