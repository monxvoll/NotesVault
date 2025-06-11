package com.notesvault.controller.crud;

import com.notesvault.model.crudLogic.ReadService;
import org.slf4j.LoggerFactory;
import com.notesvault.model.entities.Note;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/note")
//Por ahora haremos consultas  de lectura generales, usando el email
public class ReadController {
    private static final Logger logger = LoggerFactory.getLogger(ReadController.class);
    private final ReadService readService;

    public ReadController(ReadService readService) {
        this.readService = readService;
    }

    @GetMapping("/read")
    public ResponseEntity<?> readNote(@RequestParam String userEmail){
        try {
            List<Note> notes = readService.readNote(userEmail);
            logger.info("Solicitud de lectura para el usuario: {}",userEmail);
            return ResponseEntity.ok(notes);
        }catch (ResponseStatusException e){
            logger.error("Error en la consulta de las nota: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
