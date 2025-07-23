package com.notesvault.controller.auth;

import com.notesvault.dtos.DeleteAccountRequestDTO;
import com.notesvault.model.authlogic.AccountDeletionEmailService;
import com.notesvault.model.authlogic.DeleteAccountService;
import com.notesvault.model.authlogic.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController //Le decimos a springboot que esta clase es un controlador, por lo tanto devolvera una respuesta en json
@RequestMapping("/account") //Definicion Ruta Base del controlador
public class DeleteAccountController {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAccountController.class); //Para registrar eventos
    private final DeleteAccountService delete; //Servicio Injectado
    private final TokenService tokenService;
    private final AccountDeletionEmailService deletionEmailService;


    public DeleteAccountController(DeleteAccountService delete, TokenService tokenService, AccountDeletionEmailService deletionEmailService) {
        this.delete = delete;
        this.tokenService = tokenService;
        this.deletionEmailService = deletionEmailService;
    }

    @DeleteMapping("/deleteAccount")//Indicamos que en este caso el metodo responde a solicitudes HTTP delete
    public ResponseEntity<String> deleteAccount(@RequestBody DeleteAccountRequestDTO requestDTO) {
        logger.info("Solicitud de eliminación de cuenta recibida para usuario: {}", requestDTO.getEmail());
        try {
            //Verificar si la cuenta existe y no esta eliminada
            if(deletionEmailService.isAccountDeleted(requestDTO.getEmail())){
                logger.warn("No se puede reenviar token: la cuenta {} ya está eliminada o inactiva", requestDTO.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La cuenta no existe o esta inactiva");
            }

            delete.deleteAccount(requestDTO.getUserName(),requestDTO.getEmail(), requestDTO.getPassword(), requestDTO.getConfirmPassword());
            logger.info("Correo de eliminación enviado correctamente a: {}", requestDTO.getEmail());
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
            boolean isDelete = deletionEmailService.deleteAccount(token, email);
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
            //Verificar si la cuenta existe y no esta eliminada
            if(deletionEmailService.isAccountDeleted(email)){
                logger.warn("No se puede reenviar token: la cuenta {} ya está eliminada o inactiva", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La cuenta no existe o esta inactiva");
            }

            // Generar nuevo token de confirmación
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(email, "confirmation");
            logger.info("Nuevo token de eliminación generado para usuario: {}", email);

            // Enviar correo de confirmación de forma asíncrona
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
