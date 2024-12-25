package model.noteLogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import model.entities.Note;
import model.entities.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.UUID;
import java.util.Scanner;


public class Create {
        private Scanner scanner;
        private LocalDateTime localDateTime;
        private String exclusiveId;
        private Firestore firestore;

        public Create(){
            this.scanner = new Scanner(System.in);
            this.localDateTime = LocalDateTime.now();
            this.exclusiveId = UUID.randomUUID().toString();
            this.firestore = FirestoreClient.getFirestore();
        }

        public void createNote(User user){
            System.out.println("Digite el titulo de la nota");
            String title = scanner.nextLine();
            System.out.println("Digite el contenido de la nota");
            String content = scanner.nextLine();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String date = localDateTime.format(format);
            System.out.println(exclusiveId);
            Note note = new Note(title,content,date,exclusiveId);
            addNote(user,note);
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

                // Manejar el resultado de la operación asincrónica
                future.addListener(() -> {
                    try {
                        WriteResult result = future.get(); // Se guarda el resultado de la operación
                        System.out.println("Nota guardada exitosamente. Hora: " + result.getUpdateTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, Executors.newSingleThreadExecutor()); // Crear un hilo encargado de ejecutar el listener
            } else {
                System.out.println("Error: Usuario o nota no válidos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
