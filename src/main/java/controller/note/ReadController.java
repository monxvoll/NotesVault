package controller.note;

import model.entities.User;
import model.noteLogic.Read;

public class ReadController {
    private Read read;

    public ReadController (){
        this.read = new Read();
    }

    public void readNotes(User user){
        read.readNotes(user);
    }
}
