package model.crudLogic;

import com.google.cloud.firestore.*;
import model.entities.Note;
import model.entities.User;
import util.InputProvider;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Update {
    private LocalDateTime localDateTime;

    public Update(){
        this.localDateTime = LocalDateTime.now();
    }
    public void updateNote(User user , InputProvider inputProvider)  {
        try {
            List<QueryDocumentSnapshot> documents = Read.getUserNotesCollection(user);

            if (documents == null) {
                System.err.println("Error al traer las notas del usuario");
            } else {
                if (Read.hasNotes(documents)) {
                    Read.enumerateNotes(documents);

                    int noteIndex = Read.getNoteIndex(inputProvider,documents);
                    if(noteIndex== -1) return;

                    QueryDocumentSnapshot documentNote = documents.get(noteIndex );
                    Note note = documentNote.toObject(Note.class);
                    System.out.println("Titulo anterior : "+note.getTitle());
                    System.out.println("Contenido Anterior : " + note.getContent());

                    System.out.println("Por favor ingrese el nuevo titulo: ");
                    String newTitle = inputProvider.nextLine();
                    System.out.println("Por favor ingrese el nuevo contenido: ");
                    String newContent = inputProvider.nextLine();

                    if(!CreateService.checkIsNull(newTitle, newContent)) {
                        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        String date = localDateTime.format(format);

                        note.setTitle(newTitle);
                        note.setContent(newContent);
                        note.setDate(date);

                        documentNote.getReference().set(note);

                        System.err.println("Nota actualizada correctamente");
                    }
                }
            }
        }catch (NullPointerException e) {
            System.err.println("Error: No se encontraron notas o algún valor es nulo. " + e.getMessage());
            e.printStackTrace();
        } catch (FirestoreException e) {
            System.err.println("Error de Firestore al actualizar la nota: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
