package controller.auth;

import model.authlogic.DeleteAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController //Le decimos a springboot que esta clase es un controlador, por lo tanto devolvera una respuesta en json
@RequestMapping("/account") //Definicion Ruta Base del controlador
public class DeleteAccountController {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAccountController.class); //Para registrar eventos
    private final DeleteAccountService delete; //Servicio Injectado

    public DeleteAccountController(DeleteAccountService delete) {
        this.delete = delete;
    }

    @DeleteMapping("/deleteAccount")//Indicamos que en este caso el metodo responde a solicitudes HTTP delete
    public ResponseEntity<String> deleteAccount(@RequestParam String userEmail, @RequestParam String password, @RequestParam String confirmPassword, @RequestParam String confirmation) {
        logger.info("Solicitud de eliminación de cuenta recibida para usuario: {}", userEmail);
        try {
            delete.deleteAccount(userEmail, password, confirmPassword, confirmation);
            logger.info("Cuenta eliminada correctamente para usuario: {}", userEmail);
            return ResponseEntity.ok("Cuenta eliminada correctamente");
        }catch (ResponseStatusException e) {
            logger.warn("Error de validación en eliminación de cuenta: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
