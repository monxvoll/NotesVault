package controller.note;

import model.entities.User;
import model.noteLogic.Delete;

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
