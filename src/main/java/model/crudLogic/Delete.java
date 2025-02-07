package model.crudLogic;


import com.google.cloud.firestore.QueryDocumentSnapshot;
import model.entities.User;
import util.InputProvider;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Delete {

    public void removeNoteByName(User user, InputProvider inputProvider) {
        try {
            if (user == null || user.getUserName() == null) {
                System.err.println("Error: Usuario no válido.");
                return;
            }

            List<QueryDocumentSnapshot> documents = Read.getUserNotesCollection(user);

                if (documents == null) {
                    System.err.println("Error al traer las notas del usuario");
                } else {
                    if (Read.hasNotes(documents)) {
                        Read.enumerateNotes(documents);

                        int noteIndex = Read.getNoteIndex(inputProvider, documents);
                        if (noteIndex == -1) return;

                        documents.get(noteIndex).getReference().delete();
                        System.err.println("Nota borrada exitosamente");
                    }
                }
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
