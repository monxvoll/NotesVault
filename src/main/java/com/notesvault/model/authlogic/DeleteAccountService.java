package com.notesvault.model.authlogic;

import com.google.cloud.firestore.*;
import org.apache.el.parser.Token;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.ExecutionException;

@Service
public class DeleteAccountService {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAccountService.class);
    private final Firestore firestore;
    private final TokenService tokenService;
    private final AccountDeletionEmailService  accountDeletionEmailService;

    public DeleteAccountService(Firestore firestore, TokenService tokenService, AccountDeletionEmailService accountDeletionEmailService) {
        this.firestore = firestore;
        this.tokenService = tokenService;
        this.accountDeletionEmailService = accountDeletionEmailService;
    }

    public void deleteAccount(String userName, String email, String password, String confirmPassword) {
        logger.info("Solicitud de eliminación de cuenta para usuario: {}", email);

        if (isEmpty(password) || isEmpty(confirmPassword)) {
            logger.warn("Campos vacíos detectados en la solicitud de eliminación");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los campos de contraseña no pueden estar vacíos");
        }

        if (!password.equals(confirmPassword)) {
            logger.warn("Las contraseñas no coinciden para usuario: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        validatePasswordAndSendDeletionEmail(userName,password, email);
    }

    private void validatePasswordAndSendDeletionEmail(String userName, String password, String userEmail) {
        try {
            DocumentReference document = firestore.collection("users").document(userEmail);
            DocumentSnapshot snapshot = document.get().get();


            if (!snapshot.exists()) {
                logger.warn("Usuario {} no encontrado en Firestore", userEmail);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }

            String currentPassword = snapshot.getString("password");
            if (!BCrypt.checkpw(password, currentPassword)) {
                logger.warn("Contraseña incorrecta para usuario: {}", userEmail);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Contraseña incorrecta");
            }

            // Generar token de confirmación para la eliminación
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(userEmail,"confirmation");
            logger.info("Token de confirmación generado para usuario: {}",userEmail);

            // Enviar correo de eliminación de forma asíncrona
            accountDeletionEmailService.sendAccountDeletionAsync(userEmail, tokenInfo.getRawToken(),userName)
                .exceptionally(throwable -> {
                    logger.error("Error al enviar correo de eliminación a {}: {}",userEmail,throwable.getMessage());
                    return null;
                });

            logger.info("Corre de eliminación enviado exitosamente a :  {} ",userEmail);

        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al procesar la eliminación de cuenta para usuario {}: {}", userEmail, e.getMessage());
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar la cuenta", e);
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
