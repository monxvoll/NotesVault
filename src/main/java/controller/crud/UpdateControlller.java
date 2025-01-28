package controller.crud;

import model.entities.User;
import model.crudLogic.Update;
import util.InputProvider;

import java.util.Scanner;

public class UpdateControlller {
    private Update update;
    public UpdateControlller(){
      this.update = new Update();
    }
     public void updateNote(User user, Scanner scanner){
         InputProvider inputProvider = new InputProvider(scanner);
         update.updateNote(user,inputProvider);
     }
}
