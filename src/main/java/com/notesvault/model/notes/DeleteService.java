package com.notesvault.model.notes;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.ExecutionException;

@Service
public class DeleteService {
    private static final Logger logger = LoggerFactory.getLogger(DeleteService.class);
    private final Firestore firestore;

    public DeleteService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void deleteNote(String uid, String noteId) {
        try {
            logger.info("Attempting to delete note with ID {} for user {}", noteId, uid);

            // Reference to the note
            DocumentReference noteRef = firestore.collection("users").document(uid).collection("notesList").document(noteId);

            // Verify if the note exists
            ApiFuture<DocumentSnapshot> future = noteRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                logger.warn("Attempt to delete a non-existent note: {}", noteId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The note does not exist");
            }

            // Mark the note as inactive instead of deleting it
            ApiFuture<WriteResult> updateFuture = noteRef.update(
                    "active", false,
                    "deletedAt", FieldValue.serverTimestamp()
            );
            updateFuture.get();

            logger.info("Note with ID {} marked as inactive successfully.", noteId);

        } catch (InterruptedException e) {
            logger.error("Error marking the note as inactive (thread interruption): {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting note", e);

        } catch (ExecutionException e) {
            logger.error("Database error deleting note: {}", e.getCause().getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error", e);
        }
    }
}
