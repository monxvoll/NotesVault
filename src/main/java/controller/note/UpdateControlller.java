package controller.note;

import model.entities.User;
import model.noteLogic.Update;

import java.util.Scanner;

public class UpdateControlller {
    private Update update;
    public UpdateControlller(){
      this.update = new Update();
    }
     public void updateNote(User user, Scanner scanner){
         update.updateNote(user,scanner);
     }
}
