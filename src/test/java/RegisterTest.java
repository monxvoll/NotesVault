import model.authlogic.Register;
import model.entities.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.FirebaseInitializer;

public class LoginTest {
    private static User user;

    @BeforeAll
    public static void initializer(){
        FirebaseInitializer.initialize();
        user = new User("UsuarioParaPruebas", "UsuarioParaPruebas123@");
    }

    @Test
    public void testIsRegistered(){
      //  Register register = new Register();
        //register.registerUser();
    }
}
