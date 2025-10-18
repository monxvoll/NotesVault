package com.notesvault.controller.auth;

import com.notesvault.dtos.RegisterRequestDTO;
import com.notesvault.model.authlogic.ConfirmationEmailService;
import com.notesvault.model.authlogic.RegisterService;
import com.notesvault.model.authlogic.TokenService;
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
    private final ConfirmationEmailService confirmationEmailService;
    private final TokenService tokenService;

    public RegisterController(RegisterService registerService, ConfirmationEmailService confirmationEmailService, TokenService tokenService) {
        this.registerService = registerService;
        this.confirmationEmailService = confirmationEmailService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequestDTO request) {
        logger.info("Solicitud de registro recibida para usuario: {} ", request.getUserName());
        try {
            registerService.registerUser(request);
            logger.info("Registro exitoso para usuario: {} ({})", request.getUserName(), request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body("Se ha enviado un correo de confirmación.");
        } catch (ResponseStatusException e) {
            logger.error("Error en el registro para usuario {}: {} - {}", request.getEmail(), e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            logger.error("Error inesperado en el registro para usuario {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmAccount(
        @RequestParam("token") String token,
        @RequestParam("email") String email
    ){
        logger.info("Solicitud de confirmación de cuenta para usuario: {}", email);
        try{
            boolean isConfirmed = confirmationEmailService.confirmAccount(token, email);
            if(isConfirmed){
                logger.info("Cuenta confirmada exitosamente para usuario: {}", email);
                return ResponseEntity.ok("Cuenta confirmada exitosamente");
            }else{
                logger.warn("Error al confirmar la cuenta para usuario: {} - Token inválido o ya consumido", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al confirmar la cuenta. El enlace puede ser inválido o ya haber sido utilizado.");
            }
        }catch(Exception e){
            logger.error("Error al confirmar la cuenta para usuario {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor al confirmar la cuenta");
        }
    }
    
    @PostMapping("/resend-confirmation")
    public ResponseEntity<?> resendConfirmationEmail(@RequestParam("email") String email) {
        logger.info("Solicitud de reenvío de correo de confirmación para usuario: {}", email);
        try {
            // Verificar si el usuario existe
            if (!registerService.userExists(email)) {
                logger.warn("Intento de reenvío para usuario inexistente: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
            }
            
            // Verificar si el usuario ya está confirmado
            if (confirmationEmailService.isUserConfirmed(email)) {
                logger.warn("Intento de reenvío para usuario ya confirmado: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya está confirmado");
            }
            
            // Generar nuevo token de confirmación
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(email, "confirmation");
            logger.info("Nuevo token de confirmación generado para usuario: {}", email);
            
            // Enviar correo de confirmación de forma asíncrona
            confirmationEmailService.sendConfirmationEmailAsync(email, tokenInfo.getRawToken(), null)
                .exceptionally(throwable -> {
                    logger.error("Error al reenviar correo de confirmación a {}: {}", email, throwable.getMessage());
                    return null;
                });
            
            logger.info("Correo de confirmación reenviado exitosamente para usuario: {}", email);
            return ResponseEntity.ok("Correo de confirmación reenviado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al reenviar correo de confirmación para usuario {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al reenviar el correo de confirmación");
        }
    }

}
