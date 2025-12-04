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
            logger.info("Intentando leer notas del usuario {}", uid);

            //Referencia a la coleccion de notas
            CollectionReference notesRef = firestore.collection("users").document(uid).collection("notesList");

            ApiFuture<QuerySnapshot> future = notesRef.whereEqualTo("active", true).get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                logger.warn("Intento de traer notas, no realizado para: {}", uid);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay notas disponibles");
            }

            List<Note> noteList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents){
                Note note = doc.toObject(Note.class);
                noteList.add(note);
            }

            logger.info("Notas consultadas con exito para usuario {}", uid);
            return noteList;
        } catch (InterruptedException | ExecutionException e){
            logger.error("Error al procesar la consulta de notas para usuario {}: {}", uid, e.getMessage());
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al consultar las notas", e);
        }
    }

}
