package com.notesvault.controller.auth;

import com.notesvault.dtos.PasswordRecoveryRequestDTO;
import com.notesvault.dtos.PasswordResetRequestDTO;
import com.notesvault.dtos.TokenVerificationRequestDTO;
import com.notesvault.exceptions.UserNotFoundException;
import com.notesvault.model.authlogic.PasswordRecoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/recovery")
public class PasswordRecoveryController {
    private final PasswordRecoveryService passwordRecoveryService;
    private final Logger log = LoggerFactory.getLogger(PasswordRecoveryController.class);
    
    @Value("${app.recovery.base-url:http://localhost:8080}")
    private String baseUrl;

    public PasswordRecoveryController(PasswordRecoveryService passwordRecoveryService) {
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestRecovery(@RequestBody PasswordRecoveryRequestDTO requestDTO){
        try {

            passwordRecoveryService.generateRecoveryToken(requestDTO.getEmail());
            return ResponseEntity.ok().body("Correo de recuperacion enviado");

        } catch (UserNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {

            log.error("Error al procesar la solicitud de recuparecion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody TokenVerificationRequestDTO requestDTO) {
        try {
            boolean isValid = passwordRecoveryService.verifyRecoveryToken(requestDTO.getToken(), requestDTO.getEmail());
            
            if (isValid) {
                return ResponseEntity.ok().body("Token válido");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido o expirado");
            }
            
        } catch (Exception e) {
            log.error("Error al verificar token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar el token");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequestDTO requestDTO) {
        try {
            boolean success = passwordRecoveryService.changePasswordWithToken(
                requestDTO.getToken(), 
                requestDTO.getEmail(), 
                requestDTO.getNewPassword()
            );
            
            if (success) {
                return ResponseEntity.ok().body("Contraseña cambiada exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se pudo cambiar la contraseña. Verifica el token.");
            }
            
        } catch (Exception e) {
            log.error("Error al cambiar contraseña", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar la contraseña");
        }
    }

    @GetMapping("/reset")
    public ResponseEntity<?> handleRecoveryLink(
            @RequestParam("token") String token,
            @RequestParam("email") String email) {
        try {
            log.info("Verificando token de recuperación para usuario: {}", email);
            
            boolean isValid = passwordRecoveryService.verifyRecoveryToken(token, email);
            
            if (isValid) {
                // Token válido - responder con JSON
                return ResponseEntity.ok().body(Map.of(
                    "valid", true,
                    "message", "Token válido",
                    "token", token,
                    "email", email
                ));
            } else {
                // Token inválido - responder con error
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "valid", false,
                    "message", "Token inválido o expirado"
                ));
            }
            
        } catch (Exception e) {
            log.error("Error al procesar link de recuperación", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "valid", false,
                "message", "Error del servidor"
            ));
        }
    }
}
