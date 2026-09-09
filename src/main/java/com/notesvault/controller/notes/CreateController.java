package com.notesvault.controller.notes;

import com.notesvault.model.notes.CreateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;

@RestController
@RequestMapping("/note")
public class CreateController {
    private static final Logger logger = LoggerFactory.getLogger(CreateController.class);
    private final CreateService createService;

    public CreateController(CreateService createService){
        this.createService = createService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createNote(@RequestParam String title, @RequestParam String content, Principal principal){
        String uid = principal.getName();

        logger.info("Note creation request received for user: {}", uid);
        try {
            createService.createNote(uid, title, content);
            logger.info("Note creation successful for user: {}", uid);
            return ResponseEntity.status(HttpStatus.CREATED).body("Note created successfully");
        }catch (ResponseStatusException e) {
            logger.warn("Validation error in note creation: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

}
