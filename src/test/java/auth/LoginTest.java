package auth;

import com.google.cloud.firestore.Firestore;

import model.authlogic.Login;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.FirebaseInitializer;
import util.FirestoreInitializer;
import util.InputProvider;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginTest {

    private Firestore mockFirestore;
    private InputProvider inputProvider;
    private Login login;


    @BeforeAll
    public static void initializer() {
        FirebaseInitializer.initialize();
    }


    @BeforeEach
    public void setUp() {
        mockFirestore = FirestoreInitializer.getFirestore();
        inputProvider = mock(InputProvider.class);
        login = new Login(inputProvider, mockFirestore);
    }


    @Test
    public void testValidLoginUser() {
        when(inputProvider.nextLine()).thenReturn("UsuarioParaPruebas", "UsuarioParaPruebas123@");

        boolean result = login.loginUser();

        assertTrue(result, "EL resultado deberia ser true, las entradas son validas para un usuario en la base de datos ");
    }


    @Test
    public void testLoginUser_InvalidName() {
        when(inputProvider.nextLine()).thenReturn("NombreInvalido", "UsuarioParaPruebas123@");

        boolean result = login.loginUser();

        assertFalse(result, "EL resultado deberia ser false, al digitar un nombre invalido .");
    }


    @Test
    public void testLoginUser_InvalidPassword() {
        when(inputProvider.nextLine()).thenReturn("UsuarioParaPruebas", "ContraseñaInvalida");

        boolean result = login.loginUser();

        assertFalse(result, "EL resultado deberia ser false, al digitar una contraseña invalida .");
    }


    @Test
    public void testValidateInputs_InvalidNameAndInvalidPassword() {
        boolean result = login.validateInputs("NombreInvalido", "ContraseñaInvalida");

        assertFalse(result, "El resultado deberia ser false, ya que tanto el nombre y contraseña son invalidos.");
    }


    @Test
    public void testValidateInputs_NullName() {
        boolean result = login.validateInputs("", "validPassword");

        assertFalse(result, "El resultado debería ser false, ya que el nombre es null.");
    }


    @Test
    public void testValidateInputs_NullPassword() {
        boolean result = login.validateInputs("validUser", "");

        assertFalse(result, "El resultado debería ser false, ya que la contraseña es null.");
    }


    @Test
    public void testValidateInputs_NullNameAndNullPassword() {
        boolean result = login.validateInputs("", "");

        assertFalse(result, "El resultado debería ser false, ya que tanto el nombre y contraseña son null.");
    }

}
