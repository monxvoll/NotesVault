package model.authlogic;

import model.entities.User;
import org.apache.commons.validator.routines.EmailValidator;
import org.passay.*;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service // Marca la clase como un servicio
public class RegisterService {

    @Autowired
    private Firestore firestore; // Firestore se inyecta automaticamente evitando crear la instancia new..

    public void registerUser(User user) throws Exception {
        if (existsUserByName(user.getUserName())) {
            throw new IllegalArgumentException("Este nombre de usuario ya existe");
        }
        if (!validateEmail(user.getEmail())) {
            throw new IllegalArgumentException("Correo electrónico inválido");
        }
        if (!validatePassword(user.getPassword())) {
            throw new IllegalArgumentException("Contraseña inválida (8-30 caracteres, una mayúscula, un dígito y un símbolo)");
        }

        boolean success = saveUserToFirestore(user);
        if (!success) {
            throw new Exception("Error al guardar el usuario en Firestore");
        }
    }

    private boolean validateEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public boolean validatePassword(String password) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()
        ));
        return validator.validate(new PasswordData(password)).isValid();
    }

    public boolean existsUserByName(String name) {
        try {
            ApiFuture<com.google.cloud.firestore.DocumentSnapshot> future = firestore.collection("users").document(name).get();
            return future.get().exists();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al verificar el usuario en Firestore: " + e);
            return false;
        }
    }

    public boolean saveUserToFirestore(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        userMap.put("userName", user.getUserName());
        userMap.put("password", user.getPassword());

        ApiFuture<WriteResult> future = firestore.collection("users").document(user.getUserName()).set(userMap);

        try {
            WriteResult result = future.get();
            System.out.println("Usuario guardado exitosamente en Firestore en: " + result.getUpdateTime());
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error al guardar el usuario en Firestore: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
