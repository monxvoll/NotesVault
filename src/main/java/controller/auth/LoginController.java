package controller.auth;

import model.authlogic.LoginService;
import model.entities.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
        logger.info("Solicitud de inicio de sesión recibida para usuario: {}", username);
        try {
            UserDTO loggedUser = loginService.loginUser(username, password);
            logger.info("Inicio de sesión exitoso para usuario: {}", username);
            return ResponseEntity.ok(loggedUser); // Retorna el usuario (solo su nombre y email)
        } catch (IllegalArgumentException e) {
            logger.warn("Error en el inicio de sesión para usuario {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}