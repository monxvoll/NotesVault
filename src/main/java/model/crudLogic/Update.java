package model.crudLogic;

import com.google.cloud.firestore.*;
import model.entities.Note;
import model.entities.User;
import java.util.List;
import java.util.Scanner;

public class Update {

    public void updateNote(User user , Scanner scanner)  {
        try {
            List<QueryDocumentSnapshot> documents = Read.getUserNotesCollection(user);

            if (documents == null) {
                System.err.println("Error al traer las notas del usuario");
            } else {
                if (Read.hasNotes(documents)) {
                    Read.enumerateNotes(documents);

                    int noteIndex = Read.getNoteIndex(scanner,documents);
                    if(noteIndex== -1) return;

                    QueryDocumentSnapshot documentNote = documents.get(noteIndex );
                    Note note = documentNote.toObject(Note.class);
                    System.out.println("Titulo anterior : "+note.getTitle());
                    System.out.println("Contenido Anterior : " + note.getContent());

                    System.out.println("Por favor ingrese el nuevo titulo: ");
                    String newTitle = scanner.nextLine();
                    System.out.println("Por favor ingrese el nuevo contenido: ");
                    String newContent = scanner.nextLine();

                    if(!Create.checkIsNull(newTitle, newContent)) {
                        note.setTitle(newTitle);
                        note.setContent(newContent);

                        documentNote.getReference().set(note);

                        System.err.println("Nota actualizada correctamente");
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
