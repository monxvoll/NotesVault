package controller.auth;

import com.google.cloud.firestore.Firestore;
import model.authlogic.DeleteAccount;
import model.entities.User;
import util.FirestoreInitializer;
import util.InputProvider;

import java.util.Scanner;

public class DeleteAccountController {
    private DeleteAccount delete;


    public DeleteAccountController(){
        Scanner scanner = new Scanner(System.in);
        InputProvider inputProvider = new InputProvider(scanner);
        Firestore firestore = FirestoreInitializer.getFirestore();
        this.delete = new DeleteAccount(inputProvider,firestore);
    }

    public boolean deleteAccount(User user){
        return delete.deleteAccount(user);
    }
}
