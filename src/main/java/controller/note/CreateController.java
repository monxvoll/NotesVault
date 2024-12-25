package controller.note;

import model.entities.User;
import model.noteLogic.Create;

public class CreateController {
    private Create create;

    public CreateController(){
        this.create = new Create();
    }

    public void createNote(User user){
        create.createNote(user);
    }

}
