package model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import model.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import util.InputProvider;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Login {
    private Firestore firestore;
    private InputProvider inputProvider;
    private User actualUser;


    public Login(InputProvider inputProvider, Firestore firestore) {
        this.inputProvider = inputProvider;
        this.firestore = firestore;
    }

    public boolean loginUser() {
        System.out.println("Por favor digite el nombre de usuario");
        String name = inputProvider.nextLine();
        System.out.println("Por favor digite la contraseña");
        String password = inputProvider.nextLine();
        return validateInputs(name, password);
    }


    public boolean validateInputs(String name, String password) {
        if (name == null || name.isEmpty()) {
            System.err.println("El nombre es obligatorio");
        } else if (password == null || password.isEmpty()) {
            System.err.println("La contraseña es obligatoria ");
        } else {
            return compareInfo(name, password);
        }
        return false;
    }

    private boolean compareInfo(String name, String password) {
        ApiFuture<QuerySnapshot> future = firestore.collection("users").get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                String registeredUserName = document.getString("userName");
                String registeredUserPassword = document.getString("password");

                if (registeredUserName.equals(name) && BCrypt.checkpw(password, registeredUserPassword)) {
                    actualUser = new User(name, password);
                    System.err.println("Sesión iniciada correctamente");
                    return true;
                }
            }

            System.err.println("Contraseña y/o usuario incorrectos");
            return false;
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error al traer los usuarios " + e);
            return false;
        }
    }

    public User getActualUser() {
        return actualUser;
    }
}
