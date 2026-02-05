package com.notesvault.controller.auth;

import com.notesvault.dtos.LoginRequestDTO;
import com.notesvault.model.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller in charge of handling authentication requests.
 * It exposes the login endpoint to exchange credentials for a JWT.
 */

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint to authenticate a user.
     * @param loginRequest DTO containing user data
     * @return ResponseEntity with the token or an error status.
     */

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
