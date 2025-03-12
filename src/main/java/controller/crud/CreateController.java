package controller.crud;


import model.entities.User;
import model.crudLogic.CreateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/note")
public class CreateController {
    //El log llevara el nombre de la clase "CreateController"
    private static final Logger logger = LoggerFactory.getLogger(CreateController.class);
    private final CreateService createService;

    public CreateController(CreateService createService){
        this.createService = createService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createController(@RequestBody User user, @RequestParam String title, @RequestParam String content){
        logger.info("Solicitud de creacion de nota recibida para usuario: {}", user.getUserName());
        try {
            String result = createService.createNote(user, title, content);
            return ResponseEntity.ok(result);
        }catch (IllegalArgumentException e) {
            logger.warn("Error de validación en el registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
