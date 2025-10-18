package com.notesvault.model.notes;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.notesvault.model.entities.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class CreateService {
    private static final Logger logger = LoggerFactory.getLogger(CreateService.class);
    private final Firestore firestore;

    public CreateService(Firestore firestore){
        this.firestore = firestore;
    }

    public void createNote(String uid, String title, String content){
        if (validateNotEmpty(title, content)) {
            logger.info("Iniciando creación de nota para usuario: {}", uid);

            LocalDateTime localDateTime = LocalDateTime.now();
            String exclusiveId = UUID.randomUUID().toString();   //Genera un ID unico utilizando UUID y lo convierte a String
            boolean isActive = true;
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String date = localDateTime.format(format);
            Note note = new Note(title, content, date, exclusiveId,isActive);
            addNote(uid, note);
        }
    }

    private void addNote(String uid, Note note) {
        try {
            DocumentReference userRef = firestore.collection("users").document(uid);
            CollectionReference notesRef = userRef.collection("notesList");

            ApiFuture<WriteResult> future = notesRef.document(note.getId()).set(note);
            WriteResult result = future.get();

            logger.info("Nota guardada exitosamente en Firestore en: {}", result.getUpdateTime());

        } catch (InterruptedException e) {
            logger.error("Error al guardar la nota (interrupción del hilo): {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error al guardar la nota: interrupción del hilo",e);
        } catch (ExecutionException e) {
            logger.error("Error al guardar la nota en Firestore: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error al guardar la nota en Firestore",e);
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error inesperado al guardar la nota",e);
        }
    }

    public boolean validateNotEmpty(String title, String content) {
        if (title == null || title.isEmpty() || content == null || content.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Por favor, digite un campo válido");
        }
        return true;
    }
}
