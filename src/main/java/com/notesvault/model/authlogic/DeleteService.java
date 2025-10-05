package com.notesvault.model.authlogic;


import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.notesvault.model.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Service
public class DeleteService {
    private static final Logger logger = LoggerFactory.getLogger(DeleteService.class);
    private final FirebaseAuth firebaseAuth;
    private final TokenService tokenService;
    private final DeletionEmailService accountDeletionEmailService;
    private final Firestore firestore;
    private final Executor cleanupTaskExecutor;

    public DeleteService(FirebaseAuth firebaseAuth, TokenService tokenService, DeletionEmailService accountDeletionEmailService, Firestore firestore, Executor cleanupTaskExecutor) {
        this.firebaseAuth = firebaseAuth;
        this.tokenService = tokenService;
        this.accountDeletionEmailService = accountDeletionEmailService;
        this.firestore = firestore;
        this.cleanupTaskExecutor = cleanupTaskExecutor;
    }

    public void initiateAccountDeletion(String email) {
        logger.info("Solicitud para iniciar eliminacion de cuenta para: {}" + email);

        try {
            firebaseAuth.getUserByEmail(email);

            //Send token
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(email, "confirmation");
            logger.info("Token de confirmación de borrado generado para: {}", email);

            accountDeletionEmailService.sendAccountDeletionAsync(email, tokenInfo.getRawToken(), null)
                    .exceptionally(throwable -> {
                        logger.error("Error al enviar correo de eliminación a {}: {}", email, throwable.getMessage());
                        return null;
                    });

            logger.info("Correo de confirmación de borrado enviado exitosamente a: {}", email);


        } catch (FirebaseAuthException e) {
            logger.error("Error de Firebase al buscar el usuario {}: {}", email, e.getAuthErrorCode());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no fue encontrado.", e);
        }
    }

    public boolean confirmAccountDeletion(String token, String email) {
        logger.info("Confirmando la eliminación de cuenta para: {}", email);

        if (!tokenService.verifyToken(token, email, "confirmation")) {
            logger.warn("Token de eliminación inválido o ya consumido para: {}", email);
            return false;
        }
        try {
            String uid = firebaseAuth.getUserByEmail(email).getUid();

            // Disable account on firebase auth
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setDisabled(true);
            firebaseAuth.updateUser(request);
            logger.info("Cuenta del usuario {} inhabilitada en Firebase Authentication.", email);

            // Set account as disable on firestore
            firestore.collection("users").document(uid).update("isActive", false, "deletedAt", FieldValue.serverTimestamp()).get();
            logger.info("Perfil del usuario {} marcado como inactivo en Firestore.", email);

            // Clean tokens
            CompletableFuture.runAsync(() -> tokenService.deleteAllTokensForUser(email, "confirmation"), cleanupTaskExecutor);

            return true;
        } catch (Exception e) {
            logger.error("Error al procesar la eliminación de la cuenta para {}: {}", email, e.getMessage());
            return false;
        }
    }


    public boolean isAccountDeleted(String email){
        try{
            String uid = firebaseAuth.getUserByEmail(email).getUid();
            UserRecord userRecord = firebaseAuth.getUser(uid);

            // We checked both on auth both firestore
            DocumentSnapshot document= firestore.collection("users").document(uid).get().get();
            Boolean isActiveInFirestore = document.exists() ? document.getBoolean("isActive") : Boolean.FALSE;

            return userRecord.isDisabled() || Boolean.FALSE.equals(isActiveInFirestore);
            
        }catch (FirebaseAuthException e) {
            logger.error("Error de Firebase al verificar estado de eliminación para {}: {}", email, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error general al verificar estado de eliminación para {}: {}", email, e.getMessage());
            return false;
        }
    }

}
