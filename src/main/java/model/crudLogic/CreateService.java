package model.crudLogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import model.entities.Note;
import model.entities.User;
import util.InputProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;


public class Create {

    private LocalDateTime localDateTime;
    private String exclusiveId;
    private InputProvider inputProvider;
    private Firestore firestore;

    public Create(InputProvider inputProvider,Firestore firestore){
        this.inputProvider = inputProvider;
        this.localDateTime = LocalDateTime.now();
        this.exclusiveId = UUID.randomUUID().toString(); //Genera un ID unico utilizando UUID y lo convierte a String
        this.firestore = firestore;
    }

    public void createNote(User user){
        System.out.println("Digite el titulo de la nota");
        String title = inputProvider.nextLine();
        System.out.println("Digite el contenido de la nota");
        String content = inputProvider.nextLine();

        if(!checkIsNull(title, content)) {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String date = localDateTime.format(format);
            Note note = new Note(title, content, date, exclusiveId);
            addNote(user, note);
        }
    }

    private void addNote(User user, Note note) {
        try {
            // Verificar que el usuario este autenticado y su nombre de usuario no sea nulo
            if (user != null && user.getUserName() != null && note != null && note.getId() != null) {
                // Referencia al documento del usuario actual según su nombre de usuario
                DocumentReference userRef = firestore.collection("users").document(user.getUserName());

                // Referencia a la lista de notas del usuario
                CollectionReference notesRef = userRef.collection("notesList");

                // Añadir la nueva nota a la lista de notas del usuario
                ApiFuture<WriteResult> future = notesRef.document(note.getId()).set(note);
                WriteResult result = future.get();

                System.err.println("Nota guardada exitosamente en Firestore en: " + result.getUpdateTime());
            } else {
                System.out.println("Error: Usuario o nota no válidos.");
            }

        } catch (InterruptedException e) {
            System.err.println("Error al guardar la nota (interrupción del hilo): " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.err.println("Error al guardar la nota en Firestore: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean checkIsNull(String title,String content) {
        if(title.isEmpty() || content.isEmpty()){
            System.err.println("Por favor digite un campo valido");
            return true;
        }
        return false;
    }
}
