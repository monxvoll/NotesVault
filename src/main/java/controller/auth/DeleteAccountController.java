package controller.auth;

import model.authlogic.DeleteAccountService;
import model.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController //Le decimos a springboot que esta clase es un controlador, por lo tanto devolvera una respuesta en json
@RequestMapping("/account") //Definicion Ruta Base del controlador
public class DeleteAccountController {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAccountController.class); //Para registrar eventos
    private final DeleteAccountService delete; //Servicio Injectado


    public DeleteAccountController(DeleteAccountService delete) {
        this.delete = delete;
    }

    @DeleteMapping //Indicamos que en este caso el metodo responde a solicitudes HTTP delete
    public ResponseEntity<?> deleteAccount(@RequestParam User user,@RequestParam String password, @RequestParam String confirmPassword, @RequestParam String confirmation) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El cuerpo de la solicitud no puede estar vacio");
        }
        logger.info("Solicitud de eliminación de cuenta recibida para usuario: {}", user.getUserName());
        try {
            delete.deleteAccount(user, password, confirmPassword, confirmation);
            logger.info("Cuenta eliminada correctamente para usuario: {}", user.getUserName());
            return ResponseEntity.ok("Cuenta eliminada correctamente");
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación en eliminación de cuenta: {}", e.getMessage());
            //Se devolvera el estado 400 BAD REQUEST, indicando que la solicitud es invalida
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
