package controller.crud;

import model.entities.User;
import model.crudLogic.Delete;

import java.util.Scanner;

public class DeleteController {
    private Delete delete;

    public DeleteController(){
        this.delete = new Delete();
    }

    public void deleteNote(User user, Scanner scanner) {
     delete.removeNoteByName(user,scanner);
    }
}
