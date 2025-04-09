package com.notesvault.controller.crud;

import com.notesvault.model.entities.User;

public class ReadController {
    private Read read;

    public ReadController (){
        this.read = new Read();
    }

    public void readNotes(User user){
        read.readNotes(user);
    }

}
