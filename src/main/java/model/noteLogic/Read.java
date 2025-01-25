package model.noteLogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import model.entities.Note;
import model.entities.User;
import model.util.FirestoreInitializer;

import java.util.List;
import java.util.Scanner;


public class Read {

    public void readNotes(User user) {
        try {
            List<QueryDocumentSnapshot> documents = getUserNotesCollection(user);

            if(documents==null){
                System.err.println("Error al traer las notas del usuario");
            }else {
                if (hasNotes(documents)) {
                    for (QueryDocumentSnapshot document : documents) {
                        Note note = document.toObject(Note.class);
                        System.out.println(note);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void enumerateNotes(List<QueryDocumentSnapshot> documents){
        for (int i = 1; i <=documents.size() ; i++) {
            Note note = documents.get(i - 1).toObject(Note.class);
            System.out.println(i + ". " + note);
        }
    }

   public static boolean hasNotes(List<QueryDocumentSnapshot> documents) {
       if (documents.isEmpty()) {
           System.err.println("Usted actualmente no tiene notas");
           return false;
       }
       return true;
   }

    public static List<QueryDocumentSnapshot> getUserNotesCollection(User user){
        try {
            // Referencia al documento del usuario actual según su nombre de usuario
            DocumentReference userRef = FirestoreInitializer.getFirestore().collection("users").document(user.getUserName());
            //Se usa ApiFuture, que es clase que representa un resultado que estará disponible en el futuro.
            //Y QuerySnapshot, seria el contenedor de los resultados de busqueda. Es decir una lista de documentos que cumplen los requisitos de busqueda
            ApiFuture<QuerySnapshot> future = userRef.collection("notesList").get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            return documents;
        }catch (Exception e){
            e.printStackTrace();
        }
         System.out.println("aaa");
        return null;

    }

     static int getNoteIndex(Scanner scanner, List<QueryDocumentSnapshot> documents ){

        System.out.println("Por favor ingrese el número de la nota que desea actualizar : ");
        int noteIndex = scanner.nextInt();
        scanner.nextLine();

        if (noteIndex < 1 || noteIndex > documents.size()) {
            System.err.println("Por favor ingrese un número válido");
            return -1;
        }
        return noteIndex-1;
    }
}
