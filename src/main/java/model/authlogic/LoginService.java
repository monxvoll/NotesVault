package model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import model.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import util.InputProvider;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class Login {
    private Firestore firestore;


    public Login(Firestore firestore) {
        this.firestore = firestore;
    }

    public User loginUser(String name ,String password) {
        validateInputs(name, password);
        User user = compareInfo(name, password);
        if(user==null){
            throw new IllegalArgumentException("Contraseña y/o usuario incorrectos");
        }
        return user;
    }


    public void validateInputs(String name, String password) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria ");
        }
    }


    private User compareInfo(String name, String password) {
        ApiFuture<QuerySnapshot> future = firestore.collection("users").get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                String registeredUserName = document.getString("userName");
                String registeredUserPassword = document.getString("password");

                if (registeredUserName.equals(name) && BCrypt.checkpw(password, registeredUserPassword)) {
                    String email = document.getString("email");
                    return new User(email, name, password);
                }
            }
            throw new IllegalArgumentException("Contraseña y/o usuario incorrectos");
        } catch (InterruptedException e) {
            System.err.println("Error al traer los usuarios (interrupción): " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.err.println("Error al traer los usuarios: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
