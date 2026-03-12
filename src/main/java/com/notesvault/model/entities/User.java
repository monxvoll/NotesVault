package com.notesvault.model.entities;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import java.util.Date;


public class User {

    private String uid;
    private String email;
    private String userName;
    private boolean isActive;
    private boolean isConfirmed;

    @ServerTimestamp // Firestore fill this variable, automatically
    private Date createdAt;


    public User() {
        // Firestore needs an empty constructor to convert the document into a java object
    }

    public User(String uid, String email, String userName) {
        this.uid = uid;
        this.email = email;
        this.userName = userName;
        this.isActive = true;    
        this.isConfirmed = false;
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

}
