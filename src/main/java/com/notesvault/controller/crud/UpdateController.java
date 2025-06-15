package com.notesvault.controller.crud;

import com.notesvault.dtos.NoteDTO;
import com.notesvault.model.crudLogic.UpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/note")
public class UpdateController {
    private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);
    private UpdateService updateService;

    public UpdateController(UpdateService updateService) {
        this.updateService = updateService;
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> UpdateNote(@PathVariable String noteId, @RequestBody NoteDTO noteDTO){
        try{
            logger.info("Solicitud de actualizacion en la nota con id {} para el usuario {}", noteId, noteDTO.getUserEmail());
            updateService.updateNote(noteId,noteDTO);
            return ResponseEntity.ok("Nota Actualizada Correctamente");
        }catch (ResponseStatusException e){
            logger.error("Error en la actualizacion de la nota: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }
}
