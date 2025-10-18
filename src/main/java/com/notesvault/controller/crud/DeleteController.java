package com.notesvault.controller.crud;

import com.notesvault.model.notes.DeleteNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/note")
public class DeleteController {
    private static final Logger logger = LoggerFactory.getLogger(DeleteController.class);
    private final DeleteNoteService deleteService;

    public DeleteController(DeleteNoteService deleteService){
        this.deleteService = deleteService;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteNote(@RequestParam String uid,@RequestParam  String noteId) {
        logger.info("Solicitud de eliminacion para nota con ID: {}",noteId);
        try{
            deleteService.deleteNote(uid,noteId);
            logger.info("Solicitud de eliminacion exitosa para usuario: {}", uid);
            return ResponseEntity.ok("Nota eliminada exitosamente");
        } catch (ResponseStatusException e) {
            logger.error("Error en la eliminacion de la nota: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
