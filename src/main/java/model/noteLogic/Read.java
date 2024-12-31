package model.noteLogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import model.entities.Note;
import model.entities.User;

import java.util.List;


public class Read {
    private Firestore firestore;

    public Read(){
        this.firestore  = FirestoreClient.getFirestore();
    }

    public void readNotes(User user) {
        try {
            DocumentReference userRef = firestore.collection("users").document(user.getUserName());
            //Se usa ApiFuture, que es clase que representa un resultado que estará disponible en el futuro.
            //Y QuerySnapshot, seria el contenedor de los resultados de busqueda. Es decir una lista de documentos que cumplen los requisitos de busqueda
            ApiFuture<QuerySnapshot> future = userRef.collection("notesList").get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents){
                Note note = document.toObject(Note.class); //Convertir el documento a un objeto
                System.out.println(note);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
