package com.notesvault.dtos;

public class TokenVerificationRequestDTO {
    private String token;
    private String email;

    // Constructor por defecto
    public TokenVerificationRequestDTO() {}

    // Constructor con parámetros
    public TokenVerificationRequestDTO(String token, String email) {
        this.token = token;
        this.email = email;
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
} 