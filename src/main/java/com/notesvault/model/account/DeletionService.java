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
        logger.info("Solicitud para iniciar eliminacion de cuenta para: {}" + uid);

        try {
            UserRecord  userRecord = firebaseAuth.getUser(uid);
            String email = userRecord.getEmail();

            if(email==null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El usuario no tiene un email asociado.");
            }

            //Send token
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(uid, "confirmation");
            logger.info("Token de confirmación de borrado generado para: {}", email);

            accountDeletionEmailService.sendAccountDeletionAsync(email, tokenInfo.getRawToken(), null, uid)
                    .exceptionally(throwable -> {
                        logger.error("Error al enviar correo de eliminación a {}: {}", email, throwable.getMessage());
                        return null;
                    });

            logger.info("Correo de confirmación de borrado enviado exitosamente a: {}", email);


        } catch (FirebaseAuthException e) {
            logger.error("Error de Firebase al buscar el usuario {}: {}", uid, e.getAuthErrorCode());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no fue encontrado.", e);
        }
    }

    public boolean confirmAccountDeletion(String token, String uid) {
        logger.info("Confirmando la eliminación de cuenta para: {}", uid);

        if (!tokenService.verifyToken(token, uid, "confirmation")) {
            logger.warn("Token de eliminación inválido o ya consumido para: {}", uid);
            return false;
        }
        try {

            // Disable account on firebase auth
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(uid).setDisabled(true);
            firebaseAuth.updateUser(request);
            logger.info("Cuenta del usuario {} inhabilitada en Firebase Authentication.", uid);

            // Set account as disable on firestore
            firestore.collection("users").document(uid).update("active", false, "deletedAt", FieldValue.serverTimestamp()).get();
            logger.info("Perfil del usuario {} marcado como inactivo en Firestore.", uid);

            // Clean tokens
            CompletableFuture.runAsync(() -> tokenService.deleteAllTokensForUser(uid, "confirmation"), cleanupTaskExecutor);

            return true;
        } catch (Exception e) {
            logger.error("Error al procesar la eliminación de la cuenta para {}: {}", uid, e.getMessage());
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
            logger.error("Error de Firebase al verificar estado de eliminación para {}: {}", uid, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Error general al verificar estado de eliminación para {}: {}", uid, e.getMessage());
            return false;
        }
    }

}
