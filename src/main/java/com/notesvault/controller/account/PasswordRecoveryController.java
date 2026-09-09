package com.notesvault.controller.account;

import com.notesvault.dtos.PasswordRecoveryRequestDTO;
import com.notesvault.dtos.PasswordResetRequestDTO;
import com.notesvault.dtos.TokenVerificationRequestDTO;
import com.notesvault.exceptions.UserNotFoundException;
import com.notesvault.model.account.PasswordRecoveryService;
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
            return ResponseEntity.ok().body("Recovery email sent");

        } catch (UserNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {

            log.error("Error processing recovery request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody TokenVerificationRequestDTO requestDTO) {
        try {
            boolean isValid = passwordRecoveryService.verifyRecoveryToken(requestDTO.getToken(), requestDTO.getEmail());
            
            if (isValid) {
                return ResponseEntity.ok().body("Valid token");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
            }
            
        } catch (Exception e) {
            log.error("Error verifying token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error verifying token");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequestDTO requestDTO) {
        try {
            log.info("Password change request for user: {}", requestDTO.getEmail());
            
            boolean success = passwordRecoveryService.changePasswordWithToken(
                requestDTO.getToken(), 
                requestDTO.getEmail(), 
                requestDTO.getNewPassword()
            );
            
            if (success) {
                log.info("Password successfully changed for user: {}", requestDTO.getEmail());
                return ResponseEntity.ok().body("Password successfully changed");
            } else {
                log.warn("Could not change password for user: {}", requestDTO.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not change password. Verify the token.");
            }
            
        } catch (Exception e) {
            log.error("Error changing password for user {}: {}", requestDTO.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing password");
        }
    }

    @GetMapping("/reset")
    public ResponseEntity<?> handleRecoveryLink(
            @RequestParam("token") String token,
            @RequestParam("email") String email) {
        try {
            log.info("Verifying recovery token for user: {}", email);
            
            boolean isValid = passwordRecoveryService.verifyRecoveryToken(token, email);
            
            if (isValid) {
                // Valid token - respond with JSON
                return ResponseEntity.ok().body(Map.of(
                    "valid", true,
                    "message", "Valid token",
                    "token", token,
                    "email", email
                ));
            } else {
                // Invalid token - respond with error
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "valid", false,
                    "message", "Invalid or expired token"
                ));
            }
            
        } catch (Exception e) {
            log.error("Error processing recovery link", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "valid", false,
                "message", "Server error"
            ));
        }
    }
}
