package com.notesvault.model.notes;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.notesvault.model.entities.Note;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ReadService {
    private static final Logger logger = LoggerFactory.getLogger(ReadService.class);
    private final Firestore firestore;

    public ReadService(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Note> readNote(String uid){
        try {
            logger.info("Attempting to read notes for user {}", uid);

            // Reference to the notes collection
            CollectionReference notesRef = firestore.collection("users").document(uid).collection("notesList");

            ApiFuture<QuerySnapshot> future = notesRef.whereEqualTo("active", true).get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                logger.warn("Attempt to fetch notes failed for: {}", uid);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No notes available");
            }

            List<Note> noteList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents){
                Note note = doc.toObject(Note.class);
                noteList.add(note);
            }

            logger.info("Notes successfully queried for user {}", uid);
            return noteList;
        } catch (InterruptedException | ExecutionException e){
            logger.error("Error processing notes query for user {}: {}", uid, e.getMessage());
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error querying notes", e);
        }
    }

}
