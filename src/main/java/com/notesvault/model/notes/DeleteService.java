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
            logger.info("Intentando eliminar la nota con ID {} para el usuario {}", noteId, uid);

            // Referencia a la nota
            DocumentReference noteRef = firestore.collection("users").document(uid).collection("notesList").document(noteId);

            // Verificamos si la nota existe
            ApiFuture<DocumentSnapshot> future = noteRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                logger.warn("Intento de eliminar una nota inexistente: {}", noteId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La nota no existe");
            }

            // Se marca la nota como inactiva en lugar de eliminarla
            ApiFuture<WriteResult> updateFuture = noteRef.update(
                    "active", false,
                    "deletedAt", FieldValue.serverTimestamp()
            );
            updateFuture.get();

            logger.info("Nota con ID {} marcada como inactiva correctamente.", noteId);

        } catch (InterruptedException e) {
            logger.error("Error al marcar como inactiva la nota (interrupción del hilo): {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar la nota", e);

        } catch (ExecutionException e) {
            logger.error("Error en la base de datos al eliminar la nota: {}", e.getCause().getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en la base de datos", e);
        }
    }
}
