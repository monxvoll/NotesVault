package model.crudLogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import model.entities.Note;
import model.entities.User;
import util.FirestoreInitializer;
import util.InputProvider;

import java.util.List;
import java.util.concurrent.ExecutionException;


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

    public static void enumerateNotes(List<QueryDocumentSnapshot> documents){
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Restablece el estado de interrupción
            System.err.println("La operación fue interrumpida: " + e.getMessage());
            e.printStackTrace();
        } catch (ExecutionException e) {
            System.err.println("Error durante la ejecución de la consulta: " + e.getCause().getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static int getNoteIndex(InputProvider inputProvider, List<QueryDocumentSnapshot> documents ){

        System.out.println("Por favor ingrese el número de la nota que desea actualizar : ");
        int noteIndex = inputProvider.nextInt();
        inputProvider.nextLine();

        if (noteIndex < 1 || noteIndex > documents.size()) {
            System.err.println("Por favor ingrese un número válido");
            return -1;
        }
        return noteIndex-1;
    }
}
