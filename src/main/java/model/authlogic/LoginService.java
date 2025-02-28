package model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import model.entities.UserDTO;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    private final Firestore firestore;

    public LoginService(Firestore firestore) {
        this.firestore = firestore;
    }

    public UserDTO loginUser(String name , String password) {
        logger.info("Intento de inicio de sesión para usuario: {}", name);
        validateInputs(name, password);
        return compareInfo(name, password);
    }

    public void validateInputs(String name, String password) {
        if (name == null || name.isEmpty()) {
            logger.warn("Intento de inicio de sesión con nombre vacío");
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (password == null || password.isEmpty()) {
            logger.warn("Intento de inicio de sesión con contraseña vacía");
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
    }

    private UserDTO compareInfo(String name, String password) {
        ApiFuture<QuerySnapshot> future = firestore.collection("users")
                .whereEqualTo("userName", name) // Filtra directamente en Firestore
                .get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (documents.isEmpty()) {
                logger.warn("Usuario no encontrado: {}", name);
                throw new IllegalArgumentException("Usuario no encontrado");
            }

            QueryDocumentSnapshot document = documents.get(0);
            String registeredUserPassword = document.getString("password");

            if (!BCrypt.checkpw(password, registeredUserPassword)) {
                logger.warn("Contraseña incorrecta para usuario: {}", name);
                throw new IllegalArgumentException("Contraseña incorrecta");
            }

            String email = document.getString("email");
            logger.info("Inicio de sesión exitoso para usuario: {}", name);
            return new UserDTO(email, name);

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