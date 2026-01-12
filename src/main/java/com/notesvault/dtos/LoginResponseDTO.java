package com.notesvault.dtos;

/**
 * DTO assigned to handle the authentication token and basic user info
 * returned by the server after a successful login.
 */
public class LoginResponseDTO {
    private String idToken;
    private String email;
    private String uid;

    public LoginResponseDTO(String idToken, String email, String uid) {
        this.idToken = idToken;
        this.email = email;
        this.uid = uid;
    }

    // Getters
    public String getIdToken() { return idToken; }
    public String getEmail() { return email; }
    public String getUid() { return uid; }
}