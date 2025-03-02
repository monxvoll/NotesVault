package model.entities;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String email;
    private String userName;
    private String password;

    private List<Note> notesList;

    public User(String email,String userName, String password) {
        this.email = email;
        this.userName = userName;
        this.password = hashPassword(password);
        notesList = new ArrayList<>();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = hashPassword(password);
    }

    public List<Note> getNotesList() {
        return notesList;
    }

    public void setNotesList(List<Note> noteList) {
        this.notesList = noteList;
    }

    public String getEmail() {
        return email;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
