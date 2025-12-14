package com.notesvault.controller.account;

import com.notesvault.model.account.DeletionEmailService;
import com.notesvault.model.account.DeletionService;
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
    private final DeletionService deleteService;
    private final TokenService tokenService;
    private final DeletionEmailService deletionEmailService;


    public DeleteAccountController(DeletionService deleteService, TokenService tokenService, DeletionEmailService deletionEmailService) {
        this.deleteService = deleteService;
        this.tokenService = tokenService;
        this.deletionEmailService = deletionEmailService;
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<String> deleteAccount(@RequestParam String uid) {
        logger.info("Solicitud de eliminación de cuenta recibida para usuario: {}", uid);
        try {
            deleteService.initiateAccountDeletion(uid);
            logger.info("Correo de eliminación enviado correctamente a: {}", uid);
            return ResponseEntity.ok("Correo de confimación enviado exitosamente");
        }catch (ResponseStatusException e) {
            logger.warn("Error al enviar el correo de confirmación: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @GetMapping("/delete-confirmation")
    public ResponseEntity<?> deleteConfirmation(@RequestParam("token") String token,
                                                @RequestParam("uid") String uid){
        logger.info("Solicitud de confirmación de eliminacion de cuenta para usuario: {}",uid);
        try{
            boolean isDelete = deleteService.confirmAccountDeletion(token, uid);
            if(isDelete){
                logger.info("Cuenta eliminada exitosamente para usuario: {}",uid);
                return  ResponseEntity.ok("Cuenta eliminada exitosamente");
            }else {
                logger.warn("Error al intentar eliminar la cuenta para usuario: {} - Token inválido o ya consumido",uid);
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al intentar eliminar la cuenta. El enlace puede ser invalido");
            }
        }catch (Exception e){
            logger.error("Error al eliminar la cuenta para usuario {}: {}",uid,e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor al intentar la eliminacion de cuenta");
        }
    }

    @PostMapping("/resend-delete-confirmation")
    public ResponseEntity<?> resendConfirmationEmail(@RequestParam("uid") String uid){
        logger.info("Solicitud de reenvío de correo de confirmación para usuario: {}",uid);
        try{

            if(deleteService.isAccountDeleted(uid)){
                logger.warn("No se puede reenviar token: la cuenta {} ya está eliminada o inactiva", uid);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La cuenta no existe o esta inactiva");
            }

            deleteService.initiateAccountDeletion(uid);


            logger.info("Correo de eliminación reenviado exitosamente para usuario: {}", uid);
            return ResponseEntity.ok("Correo de eliminación reenviado exitosamente");

        } catch (Exception e) {
            logger.error("Error al reenviar correo de eliminación para usuario {}: {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al reenviar el correo de eliminación");
        }
    }
}
