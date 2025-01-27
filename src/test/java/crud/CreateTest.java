package crud;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import model.crudLogic.Create;
import model.entities.Note;
import model.entities.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.FirebaseInitializer;
import util.InputProvider;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CreateTest {
    private Firestore mockFirestore;
    private Create create;
    private InputProvider mockProvider;
    private User mockUser;


    @BeforeAll
    public static void initializer() {
        FirebaseInitializer.initialize();
    }


    @BeforeEach
    public void setUp(){
        mockFirestore = mock(Firestore.class);
        mockProvider = mock(InputProvider.class);
        create = new Create(mockProvider,mockFirestore);
        mockUser = new User("testUser","testPassword");
    }


    @Test
    public void testCreateNote() {
        // Se configuran las entradas del usuario usando la abstraccion de InputProvider en la clase Create
        when(mockProvider.nextLine()).thenReturn("Title", "Content");

        // Se simulan las interacciones con Firestore
        DocumentReference mockDocRef = mock(DocumentReference.class); // Simula la referencia al documento
        CollectionReference mockColRef = mock(CollectionReference.class); // Simula la referencia a la coleccion
        ApiFuture<WriteResult> mockWriteFuture = mock(ApiFuture.class); // Y simula el resultado de escritura

        when(mockFirestore.collection("users")).thenReturn(mockColRef); // Retorna la referencia a la coleccion cuando se llama a la coleccion "users"
        when(mockColRef.document(mockUser.getUserName())).thenReturn(mockDocRef); // Retorna la referencia del documento cuando se llama al documento del usuario especificado
        when(mockDocRef.collection("notesList")).thenReturn(mockColRef); // Retorna la referencia a la coleccion cuando se llama a la colección de notas del usuario
        when(mockColRef.document(anyString())).thenReturn(mockDocRef); // Retorna la referencia del documento cuando se llama a cualquier documento en la coleccion de notas del usuario
        when(mockDocRef.set(any(Note.class))).thenReturn(mockWriteFuture); // Y retorna el resultado d la operacion de escritura cuando se establece cualquier objeto Note en el documento

        create.createNote(mockUser);
    }


    @Test
    public void testValidate_validInputs(){
         boolean result = Create.checkIsNull("TituloValido","ContenidoValido");

         assertFalse(result,"El resultado deberia ser false ya que ambos parametros son validos");
    }


    @Test
    public void testValidate_NullTitle(){
        boolean result = Create.checkIsNull("","ContenidoValido");

        assertTrue(result,"El resultado deberia ser true, el titulo es invalido");
    }


    @Test
    public void testValidate_NullContent(){
        boolean result = Create.checkIsNull("TituloValido","");

        assertTrue(result,"El resultado deberia ser true, el contenido es invalido");
    }


    @Test
    public void testValidate_NullTitleAndNullContent(){
        boolean result = Create.checkIsNull("","");

        assertTrue(result,"El resultado deberia ser true, el titulo y contenido son invalidos");
    }


}
