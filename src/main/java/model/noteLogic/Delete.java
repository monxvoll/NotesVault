package model.noteLogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import model.entities.Note;
import model.entities.User;

import java.util.List;
import java.util.Scanner;


public class Delete {
    private Firestore firestore;

    public Delete(){
        this.firestore = FirestoreClient.getFirestore();
    }

    public void removeNoteByName(User user, Scanner scanner){
        System.out.println("Por favor digite el nombre de la nota a borrar ");
        String title = scanner.nextLine();
        boolean noteDeleted= false;
        try{
            DocumentReference userRef = firestore.collection("users").document(user.getUserName());

            ApiFuture<QuerySnapshot> future = userRef.collection("notesList").get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents){
                Note note = document.toObject(Note.class);

                if(note.getTitle().equals(title)) { //Los titulos duplicados se manejaran en la implementacion de la interfaz
                    document.getReference().delete(); //getReference permite acceder al documento en la base de datos
                    System.err.println("Nota borrada exitosamente");
                    noteDeleted = true;
                    break;
                }
            }
            if(!noteDeleted){
                System.err.println("No se encontro la nota con el titulo "+title);
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}
