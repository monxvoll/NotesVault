package model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
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

    public DeleteAccountService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void deleteAccount(String userEmail, String password, String confirmPassword, String confirmation) {
        logger.info("Solicitud de eliminación de cuenta para usuario: {}", userEmail);

        if (isEmpty(password) || isEmpty(confirmPassword)) {
            logger.warn("Campos vacíos detectados en la solicitud de eliminación");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los campos de contraseña no pueden estar vacíos");
        }

        if (!confirmation.equalsIgnoreCase("y")) {
            logger.info("El usuario {} canceló la eliminación de la cuenta", userEmail);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Eliminación de cuenta cancelada por el usuario");
        }

        if (!password.equals(confirmPassword)) {
            logger.warn("Las contraseñas no coinciden para usuario: {}", userEmail);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        validatePasswordAndDeactivateAccount(password, userEmail);
    }

    private void validatePasswordAndDeactivateAccount(String password, String userEmail) {
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

            // Se marca el usuario como inactivo y se guarda el timestamp de eliminación
            document.update("isActive", false, "deletedAt", FieldValue.serverTimestamp()).get();
            logger.info("Cuenta de usuario {} marcada como inactiva en Firestore", userEmail);

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
