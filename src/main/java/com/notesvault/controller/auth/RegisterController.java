package com.notesvault.controller.auth;

import com.notesvault.model.authlogic.RegisterService;
import com.notesvault.model.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        logger.info("Solicitud de registro recibida para usuario: {} ", user.getUserName());
        try {
            registerService.registerUser(user);
            logger.info("Registro exitoso para usuario: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body("Registro exitoso");
        } catch (ResponseStatusException e) {
            logger.error("Error inesperado en el registro", e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }



}
