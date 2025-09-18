package com.notesvault.model.entities;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {

    private String uid;
    private String email;
    private String userName;
    private boolean isActive;
    private boolean isConfirmed;

    @ServerTimestamp // Firestore fill this variable, automatically
    private Date createdAt;


    private List<Note> notesList;
    public User() {
        // Firestore needs an empty constructor to convert the document into a java object
    }

    public User(String uid, String email, String userName, boolean isActive, boolean isConfirmed, List<Note> notesList) {
        this.uid = uid;
        this.email = email;
        this.userName = userName;
        this.isActive = true; //Default value during registration
        this.isConfirmed = false;
        this.notesList = notesList;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        isConfirmed = confirmed;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Note> getNotesList() {
        return notesList;
    }

    public void setNotesList(List<Note> notesList) {
        this.notesList = notesList;
    }
}
