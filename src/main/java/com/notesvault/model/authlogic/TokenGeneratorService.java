package com.notesvault.model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class TokenGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(TokenGeneratorService.class);
    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final long  TOKEN_VALIDITY_HOURS = 24;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    private final Firestore firestore;

    public TokenGeneratorService(Firestore firestore) {
        this.firestore = firestore;
    }

    public GeneratedTokenInfo generateSecureToken(String email) {
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


}
