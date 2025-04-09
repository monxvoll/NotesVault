package com.notesvault.controller.auth;

import com.notesvault.model.authlogic.LoginService;
import com.notesvault.model.entities.User;
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
    public ResponseEntity<String> loginUser(@RequestBody User user) {
        logger.info("Solicitud de inicio de sesión recibida para usuario: {}", user.getUserName());
        try {
            loginService.loginUser(user);
            logger.info("Inicio de sesión exitoso para usuario: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body("Registro exitoso");
        } catch (ResponseStatusException e) {
            logger.warn("Error en el inicio de sesión para usuario {}:  {}", user.getUserName(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
