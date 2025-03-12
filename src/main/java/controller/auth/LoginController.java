package controller.auth;

import model.authlogic.LoginService;
import model.entities.User;
import model.entities.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final LoginService loginService;


    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El cuerpo de la solicitud no puede estar vacio");
        }
        logger.info("Solicitud de inicio de sesión recibida para usuario: {}", user.getUserName());
        try {
            UserDTO loggedUser = loginService.loginUser(user);
            logger.info("Inicio de sesión exitoso para usuario: {}", loggedUser);
            return ResponseEntity.ok(loggedUser); // Retorna el usuario (solo su nombre y email)
        } catch (IllegalArgumentException e) {
            logger.warn("Error en el inicio de sesión para usuario {}: {}", user.getUserName(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}