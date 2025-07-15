package com.notesvault.dtos;

public class DeleteAccountRequestDTO {
    private String email;
    private String userName;
    private String password;
    private String confirmPassword;

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}
