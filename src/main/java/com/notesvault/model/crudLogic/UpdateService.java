package com.notesvault.model.crudLogic;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.notesvault.dtos.NoteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UpdateService {
    private static final  Logger logger = LoggerFactory.getLogger(ReadService.class);
    private final Firestore firestore;

    public UpdateService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void updateNote(String noteId, NoteDTO noteDTO, String userEmail) {
        Map<String, Object> updates = new HashMap<>();
        try {
            logger.info("Intentando actualizar nota con id {} de usuario {}", noteId, userEmail);

            if(noteDTO.getTitle()!=null) updates.put("title",noteDTO.getTitle());

            if(noteDTO.getContent()!=null) updates.put("content",noteDTO.getContent());

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            updates.put("date", now.format(formatter));

            DocumentReference noteRef = firestore.collection("users").document(userEmail).collection("notesList").document(noteId);

            noteRef.update(updates).get(); //Ejecutar actualizacion

        }catch (ExecutionException | InterruptedException e) {
            logger.error("Error inesperado al intentar al actualizacion de la nota {} para el usuario {}: {}",noteId,userEmail, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error al intentar actualizar nota");
        }
    }
}
