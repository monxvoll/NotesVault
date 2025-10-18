package com.notesvault.controller.auth;

import com.notesvault.model.account.DeletionEmailService;
import com.notesvault.model.account.DeleteService;
import com.notesvault.model.account.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/account")
public class DeleteAccountController {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAccountController.class);
    private final DeleteService deleteService;
    private final TokenService tokenService;
    private final DeletionEmailService deletionEmailService;


    public DeleteAccountController(DeleteService deleteService, TokenService tokenService, DeletionEmailService deletionEmailService) {
        this.deleteService = deleteService;
        this.tokenService = tokenService;
        this.deletionEmailService = deletionEmailService;
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<String> deleteAccount(@RequestParam String email) {
        logger.info("Solicitud de eliminación de cuenta recibida para usuario: {}", email);
        try {
            deleteService.initiateAccountDeletion(email);
            logger.info("Correo de eliminación enviado correctamente a: {}", email);
            return ResponseEntity.ok("Correo de confimación enviado exitosamente");
        }catch (ResponseStatusException e) {
            logger.warn("Error al enviar el correo de confirmación: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @GetMapping("/delete-confirmation")
    public ResponseEntity<?> deleteConfirmation(@RequestParam("token") String token,
                                                @RequestParam("email") String email){
        logger.info("Solicitud de confirmación de eliminacion de cuenta para usuario: {}",email);
        try{
            boolean isDelete = deleteService.confirmAccountDeletion(token, email);
            if(isDelete){
                logger.info("Cuenta eliminada exitosamente para usuario: {}",email);
                return  ResponseEntity.ok("Cuenta eliminada exitosamente");
            }else {
                logger.warn("Error al intentar eliminar la cuenta para usuario: {} - Token inválido o ya consumido",email);
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al intentar eliminar la cuenta. El enlace puede ser invalido");
            }
        }catch (Exception e){
            logger.error("Error al eliminar la cuenta para usuario {}: {}",email,e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor al intentar la eliminacion de cuenta");
        }
    }

    @PostMapping("/resend-delete-confirmation")
    public ResponseEntity<?> resendConfirmationEmail(@RequestParam("email") String email){
        logger.info("Solicitud de reenvío de correo de confirmación para usuario: {}",email);
        try{

            if(deleteService.isAccountDeleted(email)){
                logger.warn("No se puede reenviar token: la cuenta {} ya está eliminada o inactiva", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La cuenta no existe o esta inactiva");
            }


            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(email, "confirmation");
            logger.info("Nuevo token de eliminación generado para usuario: {}", email);


            deletionEmailService.sendAccountDeletionAsync(email, tokenInfo.getRawToken(), null)
                    .exceptionally(throwable -> {
                        logger.error("Error al reenviar correo de eliminación a {}: {}", email, throwable.getMessage());
                        return null;
                    });

            logger.info("Correo de eliminación reenviado exitosamente para usuario: {}", email);
            return ResponseEntity.ok("Correo de eliminación reenviado exitosamente");

        } catch (Exception e) {
            logger.error("Error al reenviar correo de eliminación para usuario {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al reenviar el correo de eliminación");
        }
    }
}
