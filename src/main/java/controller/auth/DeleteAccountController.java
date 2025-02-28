package controller.auth;

import model.authlogic.DeleteAccountService;
import model.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/account")
public class DeleteAccountController {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAccountController.class);
    private final DeleteAccountService delete;

    @Autowired
    public DeleteAccountController(DeleteAccountService delete) {
        this.delete = delete;
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<?> deleteAccount(@RequestParam User user,@RequestParam String password, @RequestParam String confirmPassword, @RequestParam String confirmation) {

        logger.info("Solicitud de eliminación de cuenta para usuario: {}", user.getUserName());

        try {
            delete.deleteAccount(user, password, confirmPassword, confirmation);
            logger.info("Cuenta eliminada correctamente para usuario: {}", user.getUserName());
            return ResponseEntity.ok("Cuenta eliminada correctamente");
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación en eliminación de cuenta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
