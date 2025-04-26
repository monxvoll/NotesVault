package com.notesvault.dtos;

public class RegisterRequest {
    //Dto para manejar verificaciones antes de interactuar con la base de datos
    private String email;
    private String userName;
    private String password;

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
