package com.notesvault.dtos;

public class PasswordResetRequestDTO {
    private String token;
    private String email;
    private String newPassword;

    // Constructor por defecto
    public PasswordResetRequestDTO() {}

    // Constructor con parámetros
    public PasswordResetRequestDTO(String token, String email, String newPassword) {
        this.token = token;
        this.email = email;
        this.newPassword = newPassword;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 