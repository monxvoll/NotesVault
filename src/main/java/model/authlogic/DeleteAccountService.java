package model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import model.crudLogic.CreateService;
import model.entities.User;
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
    private final Firestore firestore; // final para evitar la concurrencia y modificacion por accidente

    public DeleteAccountService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void deleteAccount(User currentUser, String password, String confirmPassword, String confirmation) {
        logger.info("Solicitud de eliminación de cuenta para usuario: {}", currentUser.getEmail());

        if (CreateService.validateNotEmpty(password, confirmPassword)) {
            logger.warn("Campos vacios detectados en la solicitud de eliminación");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Por favor digite un campo válido");
        } else {
            checkData(password, confirmPassword, confirmation, currentUser);
        }
    }

    private void checkData(String password, String confirmPassword, String confirmation, User currentUser) {
        if (confirmation.equalsIgnoreCase("y")) {
            logger.info("Confirmación de eliminación recibida para usuario: {}", currentUser.getEmail());

            if (password.equals(confirmPassword)) {
                validatePassword(password, currentUser);
            } else {
                logger.warn("Las contraseñas no coinciden para usuario: {}", currentUser.getEmail());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Las contraseñas no coinciden");
            }
        } else {
            logger.info("El usuario {} canceló la eliminación de la cuenta", currentUser.getEmail());
        }
    }

    private void validatePassword(String password, User user) {
        try {
            ApiFuture<DocumentSnapshot> future = firestore.collection("users").document(user.getEmail()).get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                logger.warn("Usuario {} no encontrado en Firestore", user.getEmail());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");

            }

            String currentPassword = document.getString("password");

            if (BCrypt.checkpw(password, currentPassword)) {
                logger.info("Contraseña validada correctamente para usuario: {}", user.getEmail());
                executeElimination(user);
            } else {
                logger.warn("Contraseña incorrecta para usuario: {}", user.getEmail());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Contraseña incorrecta");
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error al obtener documento del usuario {}: {}", user.getEmail(), e.getMessage());
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener el usuario", e);
        }
    }

    private void executeElimination(User user) {
        try {
            DocumentReference document = firestore.collection("users").document(user.getEmail());

            ApiFuture<DocumentSnapshot> future = document.get();
            DocumentSnapshot snapshot = future.get();

            if (!snapshot.exists()) {
                logger.warn("No se encontró la cuenta del usuario {} en Firestore", user.getEmail());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado");
            }

            ApiFuture<WriteResult> updateFuture = document.update("isActive", false, "deletedAt", FieldValue.serverTimestamp());
            updateFuture.get();

            logger.info("Cuenta de usuario {} marcada como inactiva en Firestore", user.getEmail());
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error al marcar como inactiva la cuenta del usuario {}: {}", user.getEmail(), e.getMessage());
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar la cuenta", e);
        }
    }

}
