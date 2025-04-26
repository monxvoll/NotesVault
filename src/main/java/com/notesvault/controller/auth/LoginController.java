package com.notesvault.controller.auth;

import com.notesvault.dtos.RegisterRequest;
import com.notesvault.model.authlogic.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody RegisterRequest request) {
        logger.info("Solicitud de inicio de sesión recibida para usuario: {}", request.getEmail());
        try {
            loginService.loginUser(request);
            logger.info("Inicio de sesión exitoso para usuario: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body("Inicio de sesión exitoso");
        } catch (ResponseStatusException e) {
            logger.warn("Error en el inicio de sesión para usuario {}:  {}", request.getEmail(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
