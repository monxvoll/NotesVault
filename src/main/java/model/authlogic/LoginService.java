package model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import model.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.ExecutionException;

@Service
public class LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    private final Firestore firestore;

    public LoginService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void loginUser(User user) {
        logger.info("Intento de inicio de sesión para usuario: {}", user.getUserName());
        validateInputs(user.getEmail(),user.getPassword());
        compareCredentials(user.getEmail(), user.getPassword());
    }

    public void validateInputs(String email, String password) {
        if (email == null || email.isEmpty()) {
            logger.warn("Intento de inicio de sesión con email vacio");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El email es obligatorio");
        }
        if (password == null || password.isEmpty()) {
            logger.warn("Intento de inicio de sesión con contraseña vacía");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La contraseña es obligatoria");
        }
    }

    private void compareCredentials(String email, String password) {

        try {
            ApiFuture<DocumentSnapshot> future = firestore.collection("users").document(email).get();
            DocumentSnapshot document = future.get();
            if (!document.exists()) {
                logger.warn("Email no encontrado: {}", email);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Email no encontrado");
            }

            String registeredUserPassword = document.getString("password");

            if (!BCrypt.checkpw(password, registeredUserPassword)) {
                logger.warn("Contraseña incorrecta para : {}", email);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Contraseña incorrecta");
            }

            logger.info("Inicio de sesión exitoso para : {}", email);

        } catch (InterruptedException e) {
            logger.error("Error al traer los usuarios desde Firestore: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al traer los usuarios", e);
        } catch (ExecutionException e) {
            logger.error("Error en la ejecución al traer los usuarios: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al traer los usuarios", e);
        }
    }
}