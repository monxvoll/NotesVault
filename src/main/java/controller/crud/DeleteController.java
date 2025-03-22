package controller.crud;


import model.crudLogic.DeleteService;
import model.entities.Note;
import model.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/note")
public class DeleteController {
    private static final Logger logger = LoggerFactory.getLogger(CreateController.class);
    private final DeleteService deleteService;

    public DeleteController(DeleteService deleteService){
        this.deleteService = deleteService;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteNote(@RequestParam String userEmail,@RequestParam  String noteId) {
        logger.info("Solicitud de eliminacion para nota con ID: {}",noteId);
        try{
            deleteService.deleteNote(userEmail,noteId);
            logger.info("Solicitud de eliminacion exitosa para usuario: {}", userEmail);
            return ResponseEntity.ok("Nota eliminada exitosamente");
        } catch (ResponseStatusException e) {
            logger.error("Error en la eliminacion de la nota: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
