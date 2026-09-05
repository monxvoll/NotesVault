package com.notesvault.dtos;

public class RegisterRequestDTO {
    // DTO to handle verifications before interacting with the database
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
