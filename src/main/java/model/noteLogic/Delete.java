package model.noteLogic;


import com.google.cloud.firestore.QueryDocumentSnapshot;
import model.entities.User;

import java.util.List;
import java.util.Scanner;

public class Delete {

    public void removeNoteByName(User user, Scanner scanner){
        try {
            List<QueryDocumentSnapshot> documents = Read.getUserNotesCollection(user);

                if (documents == null) {
                    System.err.println("Error al traer las notas del usuario");
                } else {
                    if (Read.hasNotes(documents)) {
                        Read.enumerateNotes(documents);

                        int noteIndex = Read.getNoteIndex(scanner, documents);
                        if (noteIndex == -1) return;

                        documents.get(noteIndex).getReference().delete();
                        System.err.println("Nota borrada exitosamente");
                    }
                }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}
