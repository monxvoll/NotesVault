package com.notesvault.model.authlogic;

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
import java.util.concurrent.CompletableFuture;

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
     * Genera un token seguro para el usuario
     * @param email Email del usuario
     * @param type Tipo de token (confirmation, recovery)
     * @return Información del token generado
     */
    public GeneratedTokenInfo generateSecureToken(String email, String type) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Intento generar token sin email");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email del usuario es obligatorio para generar el token");
        }

        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);
        String rawToken = base64Encoder.encodeToString(randomBytes);

        Instant expirationTime = Instant.now().plus(TOKEN_VALIDITY_HOURS, ChronoUnit.HOURS);

        String hashedToken = BCrypt.hashpw(rawToken, BCrypt.gensalt());
        
        // Generar un ID único para el documento
        String documentId = UUID.randomUUID().toString();

        Map<String, Object> tokenData = new HashMap<>();

        tokenData.put("userEmail", email);
        tokenData.put("expirationTime", Timestamp.of(java.util.Date.from(expirationTime)));
        tokenData.put("hashedToken", hashedToken);
        tokenData.put("type", type);

        logger.info("Almacenando token hasheado en Firestore para usuario: {}", email);

        ApiFuture<WriteResult> writeResultFuture = firestore.collection("activeTokens").document(documentId).set(tokenData);

        try {
            WriteResult writeResult = writeResultFuture.get();
            logger.info("Token hasheado almacenado con éxito para usuario {} (UpdateTime: {})", email, writeResult.getUpdateTime());
        } catch (InterruptedException e) {
            logger.error("Interrupción durante la escritura del token en Firestore para {}: {}", email, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al almacenar el token", e);
        } catch (ExecutionException e) {
            logger.error("Error de ejecución durante la escritura del token en Firestore para {}: {}", email, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al almacenar el token", e);
        }

        return new GeneratedTokenInfo(rawToken, hashedToken, expirationTime, documentId);
    }

    /**
     * Verifica si un token es válido para un usuario y tipo específico
     * @param token Token a verificar
     * @param email Email del usuario
     * @param type Tipo de token (confirmation, recovery)
     * @return true si el token es válido, false en caso contrario
     */
    public boolean verifyToken(String token, String email, String type){
        try{
            ApiFuture<QuerySnapshot> future = firestore.collection("activeTokens")
            .whereEqualTo("userEmail", email)
            .whereEqualTo("type", type)
            .limit(1)
            .get();

            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            if (documents.isEmpty()) {
                logger.warn("No se encontraron tokens activos para el email: {}", email);
                return false;
            }

            QueryDocumentSnapshot document = documents.get(0);
            String hashedToken = document.getString("hashedToken");

            if (BCrypt.checkpw(token, hashedToken)) {
                Timestamp expirationTimestamp = document.getTimestamp("expirationTime");
                if (expirationTimestamp == null) {
                    logger.warn("Token sin timestamp de expiración para usuario: {}", email);
                    return false;
                }
                Instant expirationTime = expirationTimestamp.toDate().toInstant();
                if (Instant.now().isAfter(expirationTime)) {
                    logger.warn("Token expirado para usuario: {}", email);
                    firestore.collection("activeTokens").document(document.getId()).delete();
                    return false;
                }

                logger.info("Token verificado exitosamente para usuario: {}", email);
                return true;
            }

            logger.warn("Token no válido para el email: {}", email);
            return false;
        } catch (Exception e) {
            logger.error("Error al verificar token", e);
            return false;
        }
    }

    /**
     * Verifica y consume un token (lo elimina después de verificar)
     * @param token Token a verificar
     * @param email Email del usuario
     * @param type Tipo de token (confirmation, recovery)
     * @return true si el token es válido y fue consumido, false en caso contrario
     */
    public boolean  verifyAndConsumeToken(String token, String email, String type){
        try{
            ApiFuture<QuerySnapshot> future = firestore.collection("activeTokens")
            .whereEqualTo("userEmail", email)
            .whereEqualTo("type", type)
            .limit(1)
            .get();

            QuerySnapshot querySnapshot = future.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            if (documents.isEmpty()) {
                logger.warn("No se encontraron tokens activos para el email: {}", email);
                return false;
            }

            QueryDocumentSnapshot document = documents.get(0);
            String hashedToken = document.getString("hashedToken");

            if (BCrypt.checkpw(token, hashedToken)) {
                Timestamp expirationTimestamp = document.getTimestamp("expirationTime");
                if (expirationTimestamp == null) {
                    logger.warn("Token sin timestamp de expiración para usuario: {}", email);
                    return false;
                }
                Instant expirationTime = expirationTimestamp.toDate().toInstant();
                if (Instant.now().isAfter(expirationTime)) {
                    logger.warn("Token expirado para usuario: {}", email);
                    firestore.collection("activeTokens").document(document.getId()).delete();
                    return false;
                }

                // Token válido - eliminarlo después de verificar
                firestore.collection("activeTokens").document(document.getId()).delete();
                logger.info("Token verificado y consumido exitosamente para usuario: {}", email);
                return true;
            }

            logger.warn("Token no válido para el email: {}", email);
            return false;
        } catch (Exception e) {
            logger.error("Error al verificar y consumir token", e);
            return false;
        }
    }

    /**
     * Elimina todos los tokens activos para un usuario específico
     * @param email Email del usuario
     * @param type Tipo de token (confirmation, recovery)
     * @return true si se eliminaron tokens, false en caso contrario
     */
    public boolean deleteAllTokensForUser(String email, String type) {
        try {
            ApiFuture<QuerySnapshot> tokensFuture = firestore.collection("activeTokens")
                .whereEqualTo("userEmail", email)
                .whereEqualTo("type", type)
                .get();
            
            QuerySnapshot tokensSnapshot = tokensFuture.get();
            int deletedCount = 0;
            
            for (QueryDocumentSnapshot document : tokensSnapshot.getDocuments()) {
                firestore.collection("activeTokens").document(document.getId()).delete();
                deletedCount++;
            }
            
            logger.info("Eliminados {} tokens para usuario: {} (tipo: {})", deletedCount, email, type);
            return deletedCount > 0;
            
        } catch (Exception e) {
            logger.error("Error al eliminar tokens para usuario {}: {}", email, e.getMessage());
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
            
            logger.info("Eliminados {} tokens expirados", deletedCount);
        } catch (Exception e) {
            logger.error("Error al limpiar tokens expirados", e);
        }
    }
}
