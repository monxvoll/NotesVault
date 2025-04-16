package com.notesvault.model.entities;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String email;
    private String userName;
    private String password;

    private List<Note> notesList;
    public User(){

    }
    public User(String email,String userName, String password) {
        this.email = email;
        this.userName = userName;
        this.password = hashPassword(password);
        notesList = new ArrayList<>();
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
