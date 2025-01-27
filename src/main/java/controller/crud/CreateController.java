package controller.crud;

import com.google.cloud.firestore.Firestore;
import model.entities.User;
import model.noteLogic.Create;
import util.FirestoreInitializer;

import java.util.Scanner;

public class CreateController {
    private Create create;

    public CreateController(){
        Firestore firestore = FirestoreInitializer.getFirestore();
        this.create = new Create(firestore);
    }

    public void createNote(User user, Scanner scanner){
        create.createNote(user,scanner);
    }

}
