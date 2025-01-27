package CrudTest;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import model.entities.User;
import model.noteLogic.Read;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.FirebaseInitializer;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ReadTest {
    private static User user;

    @BeforeAll
    public static void initializer(){
        FirebaseInitializer.initialize(); //inicializa firebase una sola vez ants de cada prueba
        user = new User("UsuarioParaPruebas", "UsuarioParaPruebas123@");
    }

    @Test
    public void testGetUserNotesCollection(){
        List<QueryDocumentSnapshot> documents = Read.getUserNotesCollection(user);
        assertNotNull(documents,"La coleccion de notas no debe ser null");
    }

    @Test
    public void testReadNotes(){
        Read read = new Read();
        read.readNotes(user);
        // salida en consola
    }

    @Test
    public void testHasNotes(){
        List<QueryDocumentSnapshot> emptyList = List.of(); //lista vacia de documentos
        assertFalse(Read.hasNotes(emptyList),"La lista no deberia tener notas");

        QueryDocumentSnapshot mockDocument = mock(QueryDocumentSnapshot.class); //se usa mockito para crear un objeto simulado "mockDocument"
        List<QueryDocumentSnapshot> list = List.of(mockDocument); //lista que contiene notas simuladas
        assertTrue(Read.hasNotes(list),"La lista deberia tener notas");
    }

}
