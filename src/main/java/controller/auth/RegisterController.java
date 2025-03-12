package controller.auth;

import model.authlogic.RegisterService;
import model.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private final RegisterService registerService;
    
    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El cuerpo de la solicitud no puede estar vacio");
        }

        try {
            registerService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("Registro exitoso");
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación en el registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado en el registro", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
        }
    }



}
