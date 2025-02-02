package model.authlogic;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import model.crudLogic.Create;
import model.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import util.InputProvider;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class DeleteAccount {
    private Firestore firestore;
    private InputProvider inputProvider;

   public DeleteAccount(InputProvider provider,Firestore firestore){
       this.inputProvider = provider;
       this.firestore = firestore;
   }

   public boolean deleteAccount(User currentUser){
       System.out.println("Por favor, ingrese su contraseña para confirmar la eliminación de su cuenta : ");
       String password = inputProvider.nextLine();
       System.out.println("Por favor confirme la contraseña");
       String confirmPassword = inputProvider.nextLine();
        if(!Create.checkIsNull(password,confirmPassword)) {
            System.err.println("Tenga en cuenta que, una vez que su cuenta sea eliminada, no podrá acceder a sus notas ni recuperar ningún dato asociado a ella.");
            System.out.println("Esperando confirmacion y/n");
            String confirmation = inputProvider.nextLine();

            return checkData(password, confirmPassword, confirmation, currentUser);
        }else {
            return false;
        }
   }



   private boolean checkData(String password,String confirmPassword,String confirmation,User currentUser){
        if(confirmation.equals("y")){
            if(password.equals(confirmPassword)){
                return validatePassword(password,currentUser);
            }else {
                System.err.println("Las contraseñas no coinciden");
                return false;
            }
        }else {
            return false;
        }
   }



    private boolean validatePassword(String password,User user){
        ApiFuture<DocumentSnapshot> future = firestore.collection("users").document(user.getUserName()).get();
        try{
            DocumentSnapshot document = future.get();
            String currentPassword = document.getString("password");
            if(BCrypt.checkpw(password, currentPassword)){
                return executeElimination(user);
            }else {
                System.err.println("Contraseña incorrecta");
                return false;
            }
        }catch (ExecutionException | InterruptedException e) {
            System.err.println("Error al obtener documento del usuario: " + e.getMessage());
        }
        return false;
    }



   private boolean executeElimination(User user){
       CollectionReference notesCollection = firestore.collection("users").document(user.getUserName()).collection("notesList");
       ApiFuture<QuerySnapshot> future = notesCollection.get();
        //Eliminar subcolecciones
       try {
          List<QueryDocumentSnapshot> documents = future.get().getDocuments();

           for (QueryDocumentSnapshot document : documents){
               document.getReference().delete();
           }
           //Eliminar documento
           DocumentReference document  = firestore.collection("users").document(user.getUserName());
           document.delete();

           System.out.println("Cuenta eliminada exitosamente");
            return true;
       } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error al eliminar el usuario: " + e.getMessage());
       }
       return false;
   }
}
