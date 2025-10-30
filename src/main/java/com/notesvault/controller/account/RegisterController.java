package com.notesvault.controller.account;

import com.notesvault.dtos.RegisterRequestDTO;
import com.notesvault.model.account.ConfirmationEmailService;
import com.notesvault.model.account.RegisterService;
import com.notesvault.model.account.TokenService;
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
            logger.info("Registro exitoso para usuario: {}", request.getUserName());
            return ResponseEntity.status(HttpStatus.CREATED).body("Se ha enviado un correo de confirmación.");
        } catch (ResponseStatusException e) {
            logger.error("Error en el registro para usuario {}: {} - {}", request.getUserName(), e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            logger.error("Error inesperado en el registro para usuario {}: {}", request.getUserName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmAccount(
        @RequestParam("token") String token,
        @RequestParam("uid") String uid
    ){
        logger.info("Solicitud de confirmación de cuenta para usuario: {}", uid);
        try{
            boolean isConfirmed = confirmationEmailService.confirmAccount(token, uid);
            if(isConfirmed){
                logger.info("Cuenta confirmada exitosamente para usuario: {}", uid);
                return ResponseEntity.ok("Cuenta confirmada exitosamente");
            }else{
                logger.warn("Error al confirmar la cuenta para usuario: {} - Token inválido o ya consumido", uid);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al confirmar la cuenta. El enlace puede ser inválido o ya haber sido utilizado.");
            }
        }catch(Exception e){
            logger.error("Error al confirmar la cuenta para usuario {}: {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor al confirmar la cuenta");
        }
    }
    
    @PostMapping("/resend-confirmation")
    public ResponseEntity<?> resendConfirmationEmail(@RequestParam("uid") String uid, @RequestParam("email") String email) {
        logger.info("Solicitud de reenvío de correo de confirmación para usuario: {}", uid);
        try {

            // Verificar si el usuario ya está confirmado
            if (confirmationEmailService.isUserConfirmed(uid)) {
                logger.warn("Intento de reenvío para usuario ya confirmado: {}", uid);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya está confirmado");
            }
            
            // Generar nuevo token de confirmación
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(uid, "confirmation");
            logger.info("Nuevo token de confirmación generado para usuario: {}", uid);
            
            // Enviar correo de confirmación de forma asíncrona
            confirmationEmailService.sendConfirmationEmailAsync(uid , email, tokenInfo.getRawToken(), null)
                .exceptionally(throwable -> {
                    logger.error("Error al reenviar correo de confirmación a {}: {}", uid, throwable.getMessage());
                    return null;
                });
            
            logger.info("Correo de confirmación reenviado exitosamente para usuario: {}", uid);
            return ResponseEntity.ok("Correo de confirmación reenviado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al reenviar correo de confirmación para usuario {}: {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al reenviar el correo de confirmación");
        }
    }
}
