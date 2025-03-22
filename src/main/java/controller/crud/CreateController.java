package controller.crud;

import model.crudLogic.CreateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/note")
public class CreateController {
    private static final Logger logger = LoggerFactory.getLogger(CreateController.class);
    private final CreateService createService;

    public CreateController(CreateService createService){
        this.createService = createService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createNote(@RequestParam String userEmail, @RequestParam String title, @RequestParam String content){
        logger.info("Solicitud de creacion de nota recibida para usuario: {}", userEmail);
        try {
            createService.createNote(userEmail, title, content);
            logger.info("Solicitud de creacion de nota exitosa para usuario: {}", userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body("Registro exitoso");
        }catch (ResponseStatusException e) {
            logger.warn("Error de validación en el registro: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

}
