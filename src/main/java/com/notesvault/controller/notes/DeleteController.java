package com.notesvault.controller.notes;

import com.notesvault.model.notes.DeleteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;

@RestController
@RequestMapping("/note")
public class DeleteController {
    private static final Logger logger = LoggerFactory.getLogger(DeleteController.class);
    private final DeleteService deleteService;

    public DeleteController(DeleteService deleteService){
        this.deleteService = deleteService;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteNote(@RequestParam  String noteId, Principal principal) {
        String uid = principal.getName();

        logger.info("Deletion request for note with ID: {}",noteId);
        try{
            deleteService.deleteNote(uid,noteId);
            logger.info("Deletion successful for user: {}", uid);
            return ResponseEntity.ok("Note successfully deleted");
        } catch (ResponseStatusException e) {
            logger.error("Error deleting note: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
