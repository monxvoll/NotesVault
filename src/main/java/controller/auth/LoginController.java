package controller.auth;

import com.google.cloud.firestore.Firestore;
import model.authlogic.Login;
import model.entities.User;
import util.FirestoreInitializer;

import java.util.Scanner;

public class LoginController {
        private Login login ;

        public LoginController() {
            Scanner scanner = new Scanner(System.in);
            Firestore firestore = FirestoreInitializer.getFirestore();
            this.login = new Login(scanner,firestore);
        }

        public boolean login() {
            return login.loginUser();
        }

        public User getActualUser(){
            return login.getActualUser();
        }
}
