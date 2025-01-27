package controller.crud;

import com.google.cloud.firestore.Firestore;
import model.entities.User;
import model.crudLogic.Create;
import util.FirestoreInitializer;
import util.InputProvider;

import java.util.Scanner;

public class CreateController {
    private Create create;

    public CreateController(){
        Scanner scanner = new Scanner(System.in);
        InputProvider inputProvider = new InputProvider(scanner);
        Firestore firestore = FirestoreInitializer.getFirestore();
        this.create = new Create(inputProvider,firestore);
    }

    public void createNote(User user){
        create.createNote(user);
    }

}
