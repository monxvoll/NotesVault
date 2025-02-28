package model.authlogic;

import model.entities.User;
import org.apache.commons.validator.routines.EmailValidator;
import org.passay.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service // Marca la clase como un servicio
public class RegisterService {
    //Logger para rastrear eventos y errores en la clase actual
    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);
    private final Firestore firestore;

    public RegisterService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void registerUser(User user) throws Exception {
        logger.info("Intentando registrar usuario: {}", user.getUserName());

        if (existsUserByName(user.getUserName())) {
            logger.warn("Intento de registro con nombre de usuario ya existente: {}", user.getUserName());
            throw new IllegalArgumentException("Este nombre de usuario ya existe");
        }
        if (!validateEmail(user.getEmail())) {
            logger.warn("Intento de registro con correo inválido: {}", user.getEmail());
            throw new IllegalArgumentException("Correo electrónico inválido");
        }
        if (!validatePassword(user.getPassword())) {
            logger.warn("Intento de registro con contraseña no válida para usuario: {}", user.getUserName());
            throw new IllegalArgumentException("Contraseña inválida (8-30 caracteres, una mayúscula, un dígito y un símbolo)");
        }

        boolean success = saveUserToFirestore(user);
        if (!success) {
            logger.error("Fallo al guardar usuario en Firestore: {}", user.getUserName());
            throw new Exception("Error al guardar el usuario en Firestore");
        }

        logger.info("Usuario {} registrado exitosamente en Firestore", user.getUserName());
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
            boolean exists = future.get().exists();
            logger.debug("Verificación de usuario en Firestore: {} - Existe: {}", name, exists);
            return exists;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error al verificar el usuario en Firestore: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
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
            logger.info("Usuario guardado exitosamente en Firestore en: {}", result.getUpdateTime());
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error al guardar el usuario en Firestore: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
