package model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import model.entities.User;
import org.mindrot.jbcrypt.BCrypt;


import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Login {
    private Firestore firestore;
    private Scanner scanner ;
    private User actualUser;

    public Login (){
        this.firestore = FirestoreClient.getFirestore(); // Inicializa Firestore
        this.scanner = new Scanner(System.in);
    }

    public boolean loginUser(){
        System.out.println("Por favor digite el nombre de usuario");
        String name = scanner.nextLine();
        System.out.println("Por favor digite la contraseña");
        String password = scanner.nextLine();
        return validateInputs(name, password);
    }

    private boolean validateInputs(String name,String password){

        if(name==null||name.isEmpty())  {
            System.err.println("El nombre es obligatorio");
        }else if (password==null||password.isEmpty()) {
            System.err.println("La contraseña es obligatoria ");
        }else {
           return compareInfo(name, password);
        }
        return false;
    }

    private boolean compareInfo(String name , String password){
        ApiFuture<QuerySnapshot> future = firestore.collection("users").get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents){
                String registeredUserName = document.getString("userName");
                String registeredUserPassword = document.getString("password");

                if (registeredUserName.equals(name) && BCrypt.checkpw(password, registeredUserPassword)) {
                    actualUser = new User(name,password);
                    System.err.println("Sesion iniciada correctamente");
                     return true;
                }
            }

            System.err.println("Contraseña y/o usuario incorrectos");
            return false;

        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error al traer los usuarios "+e);
            return false;
        }
    }

    public User getActualUser(){
        return actualUser;
    }
}
