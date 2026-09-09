package com.notesvault.controller.notes;

import com.notesvault.model.notes.ReadService;
import org.slf4j.LoggerFactory;
import com.notesvault.model.entities.Note;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/note")
// For now we will do general read queries, using the email
public class ReadController {
    private static final Logger logger = LoggerFactory.getLogger(ReadController.class);
    private final ReadService readService;

    public ReadController(ReadService readService) {
        this.readService = readService;
    }

    @GetMapping("/read")
    public ResponseEntity<?> readNote(Principal principal){
        String uid = principal.getName(); // Get uid from token

        try {
            List<Note> notes = readService.readNote(uid);
            logger.info("Read request for user: {}",uid);
            return ResponseEntity.ok(notes);
        }catch (ResponseStatusException e){
            logger.error("Error querying notes: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
