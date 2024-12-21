package controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.mindrot.jbcrypt.BCrypt;


import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class LoginController {
        private Firestore db;
        private Scanner scanner ;
        public LoginController (){
                this.db = FirestoreClient.getFirestore(); // Inicializa Firestore
                this.scanner = new Scanner(System.in);
        }

        public void login(){
                System.out.println("Por favor digite el nombre de usuario");
                String name = scanner.nextLine();
                System.out.println("Por favor digite la contraseña");
                String password = scanner.nextLine();
                validateInput(name,password);
        }

        private void validateInput(String name,String password){
                if(name==null||name.isEmpty())  {
                        System.err.println("El nombre es obligatorio");
                }else if (password==null||password.isEmpty()) {
                        System.err.println("La contraseña es obligatoria ");
                }else {
                        compareInfo(name, password);
                }
        }
        private void compareInfo(String name , String password){
                ApiFuture<QuerySnapshot> future = db.collection("users").get();
                try {
                        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                        boolean userFound = false;
                        for (QueryDocumentSnapshot document : documents){
                                String registeredUserName = document.getString("userName");
                                String registeredUserPassword = document.getString("password");

                                if (registeredUserName.equals(name) && BCrypt.checkpw(password, registeredUserPassword)) {
                                        userFound = true;
                                        System.err.println("Sesion iniciada correctamente");
                                        break;
                                }else {
                                        userFound = true;
                                        System.err.println("Contraseña y/o usuario incorrectos");
                                        break;
                                }
                        }
                        if(!userFound){
                                System.err.println("No hemos podido encontrar ninguna cuenta con ese nombre de usuario");
                        }

                } catch (ExecutionException | InterruptedException e) {
                        System.err.println("Error al traer los usuarios "+e);
                }

        }

}
