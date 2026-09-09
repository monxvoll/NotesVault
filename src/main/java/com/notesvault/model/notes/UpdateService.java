package com.notesvault.model.notes;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
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

    public void updateNote(String noteId, NoteDTO noteDTO, String uid) {
        Map<String, Object> updates = new HashMap<>();
        try {
            logger.info("Attempting to update note with id {} for user {}", noteId, uid);

            DocumentReference noteRef = firestore.collection("users").document(uid).collection("notesList").document(noteId);
            //Read Document
            DocumentSnapshot document = noteRef.get().get();

            if (!document.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The note does not exist");
            }

            Boolean isActive = document.getBoolean("active");


            if (isActive == null || !isActive) {
                logger.warn("Attempt to update inactive note: {}", noteId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The note is not available");
            }

            if(noteDTO.getTitle()!=null) updates.put("title",noteDTO.getTitle());

            if(noteDTO.getContent()!=null) updates.put("content",noteDTO.getContent());

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            updates.put("date", now.format(formatter));


            noteRef.update(updates).get();
            logger.info("Note updated successfully");

        }catch (ExecutionException | InterruptedException e) {
            logger.error("Unexpected error attempting to update note {} for user {}: {}",noteId,uid, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error attempting to update note");
        }
    }
}
