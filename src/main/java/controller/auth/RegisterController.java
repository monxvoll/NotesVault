package controller.auth;


import com.google.cloud.firestore.Firestore;
import model.authlogic.Register;
import util.FirestoreInitializer;

import java.util.Scanner;

public class RegisterController {
    private Register register;

    public RegisterController (){
        Firestore firestore = FirestoreInitializer.getFirestore();
        Scanner scanner = new Scanner(System.in);
        this.register = new Register(scanner,firestore);
    }

    public void register(){
        register.registerUser();
    }
}
