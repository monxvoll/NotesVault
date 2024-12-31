package controller.note;

import model.entities.User;
import model.noteLogic.Create;

import java.util.Scanner;

public class CreateController {
    private Create create;

    public CreateController(){
        this.create = new Create();
    }

    public void createNote(User user, Scanner scanner){
        create.createNote(user,scanner);
    }

}
