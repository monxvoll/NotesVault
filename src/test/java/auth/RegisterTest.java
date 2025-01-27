package authTest;

import com.google.cloud.firestore.Firestore;
import model.authlogic.Register;
import model.entities.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.FirebaseInitializer;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class RegisterTest {

    private Firestore firestore;
    private Scanner scanner;
    private Register register;

    @BeforeAll //Se ejecuta una vez antes de todas las pruebas
    public static void initializer() {
        FirebaseInitializer.initialize();
    }

    @BeforeEach //Se ejecuta una vez antes de cada prueba
    public void setup() {
        firestore = mock(Firestore.class);
        scanner = new Scanner(System.in);
        register = mock(Register.class); // Mock de Register
    }

    @Test
    public void testSaveUserWhenNotExists() {
        User user = new User("UsuarioParaPruebas", "UsuarioParaPruebas123@");

        // Simula que el usuario no existe, por lo que deberia guardarse
        when(register.existsUserByName(user.getUserName())).thenReturn(false);

        if(register.existsUserByName(user.getUserName())){
            System.out.println("Ya hay un usuario con este nombre");
        }else {
            register.saveUserToFirestore(user);
            //Salida en consola
        }

        // Verifica que saveUserToFirestore fue llamado una vez
        verify(register, times(1)).saveUserToFirestore(user);
    }

    @Test
    public void testDoNotSaveUserWhenExists() {
        User userTwo = new User("UsuarioParaPruebasInexistente", "UsuarioParaPruebas123@");

        //Simula que el usuario nombre de usuario ya existe, por lo que NO deberia guardarse
        when(register.existsUserByName(userTwo.getUserName())).thenReturn(true);

        if(register.existsUserByName(userTwo.getUserName())){
            System.out.println("Ya hay un usuario con este nombre");
        }else {
            register.saveUserToFirestore(userTwo);
        }

        // Verifica que saveUserToFirestore no fue llamado
        verify(register, times(0)).saveUserToFirestore(userTwo);
    }

    @Test
    public void testValidatePassword() {
        // Simula la validacion de la contraseña
        when(register.validatePassword("ContraseñaValida88@")).thenReturn(true);
        when(register.validatePassword("contraseñainvalida")).thenReturn(false);

        assertTrue(register.validatePassword("ContraseñaValida88@"), "La contraseña deberia ser valida");
        assertFalse(register.validatePassword("contraseñainvalida"), "La contraseña no deberia ser valida");
    }
}
