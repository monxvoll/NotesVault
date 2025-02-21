package controller.auth;

import model.authlogic.RegisterService;
import model.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Marca la clase como un controlador REST, es decir que indica que los métodos dentro de la clase devuelven respuestas HTTP en formato json
@RequestMapping("/auth") // Se define la ruta base a usar  para este controlador
public class RegisterController {

    private final RegisterService registerService;
    @Autowired
    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    /**
     * Se maneja la solicitud de registro de un usuario.
     *
     * @param user Objeto User enviado en el cuerpo de la solicitud.
     * @return ResponseEntity con el resultado del registro.
     */
    @PostMapping("/register") // Define que este método responde a solicitudes POST en /auth/register
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            registerService.registerUser(user); 
            return ResponseEntity.status(HttpStatus.CREATED).body("Registro exitoso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
        }
    }
}
