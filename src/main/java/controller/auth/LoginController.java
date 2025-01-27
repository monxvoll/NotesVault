package controller.auth;

import com.google.cloud.firestore.Firestore;
import model.authlogic.Login;
import model.entities.User;
import util.FirestoreInitializer;
import util.InputProvider;

import java.util.Scanner;

public class LoginController {
        private Login login ;

        public LoginController() {
            Scanner scanner = new Scanner(System.in);
            InputProvider inputProvider = new InputProvider(scanner);
            Firestore firestore = FirestoreInitializer.getFirestore();
            this.login = new Login(inputProvider,firestore);
        }

        public boolean login() {
            return login.loginUser();
        }

        public User getActualUser(){
            return login.getActualUser();
        }
}
