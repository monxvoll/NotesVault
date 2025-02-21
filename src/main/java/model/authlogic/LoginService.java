package model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import model.entities.User;
import model.entities.UserDTO;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class LoginService {
    private Firestore firestore;


    public LoginService(Firestore firestore) {
        this.firestore = firestore;
    }

    public UserDTO loginUser(String name , String password) {
        validateInputs(name, password);
        return compareInfo(name, password);
    }


    public void validateInputs(String name, String password) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria ");
        }
    }


    private UserDTO compareInfo(String name, String password) {
        ApiFuture<QuerySnapshot> future = firestore.collection("users")
                .whereEqualTo("userName", name) //Ahora se filtra directamente en Firestore
                .get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (documents.isEmpty()) {
                throw new IllegalArgumentException("Usuario no encontrado");
            }

            QueryDocumentSnapshot document = documents.get(0);
            String registeredUserPassword = document.getString("password");

            if (!BCrypt.checkpw(password, registeredUserPassword)) {
                throw new IllegalArgumentException("Contraseña incorrecta");
            }

            String email = document.getString("email");
            return new UserDTO(email, name);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al traer los usuarios", e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al traer los usuarios", e);
        }
    }

}
