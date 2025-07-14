package com.notesvault.model.authlogic;

import com.google.cloud.firestore.FieldValue;
import com.notesvault.dtos.RegisterRequest;
import com.notesvault.model.entities.User;
import org.apache.commons.validator.routines.EmailValidator;
import org.passay.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service // Marca la clase como un servicio
public class RegisterService {
    //Logger para rastrear eventos y errores en la clase actual
    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);
    private final Firestore firestore;
    private final ConfirmationEmailService confirmationEmailService;
    private final TokenService tokenService;

    public RegisterService(Firestore firestore, ConfirmationEmailService confirmationEmailService, TokenService tokenService) {
        this.firestore = firestore;
        this.confirmationEmailService = confirmationEmailService;
        this.tokenService = tokenService;
    }

    public void registerUser(RegisterRequest request)  {
        logger.info("Intentando registrar usuario: {} ({})", request.getUserName(), request.getEmail());
        String email = request.getEmail();
        String password = request.getPassword();

        if (existsUserByEmail(email)) {
            logger.warn("Intento de registro con email ya existente: {}", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Este email ya se encuentra registrado");
        }
        if (!validateEmail(email)) {
            logger.warn("Intento de registro con correo inválido: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Correo electrónico invalido");
        }

        if (!validatePassword(password)) {
            logger.warn("Intento de registro con contraseña no válida para usuario: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Contraseña inválida (8-30 caracteres, una mayúscula, un dígito,sin espacios y un símbolo)");
        }
        
        //Se hashea la contraseña despues de hacer la validación
        User user = new User(request.getEmail(), request.getUserName(), password);
        boolean success = saveUserToFirestore(user);
        if (!success) {
            logger.error("Fallo al guardar usuario en Firestore: {}", user.getEmail());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error al guardar el usuario en Firestore");
        }
        
        // Generar token de confirmación
        TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(user.getEmail(), "confirmation");
        logger.info("Token de confirmación generado para usuario: {}", user.getEmail());
        
        // Enviar correo de confirmación de forma asíncrona (no bloquea la respuesta)
        confirmationEmailService.sendConfirmationEmailAsync(user.getEmail(), tokenInfo.getRawToken(), user.getUserName())
            .exceptionally(throwable -> {
                logger.error("Error al enviar correo de confirmación a {}: {}", user.getEmail(), throwable.getMessage());
                return null;
            });
            
        logger.info("Usuario {} registrado exitosamente en Firestore", user.getEmail());
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

    public boolean existsUserByEmail(String email) {
        try {
            ApiFuture<com.google.cloud.firestore.DocumentSnapshot> future = firestore.collection("users").document(email).get();
            boolean exists = future.get().exists();
            logger.debug("Verificación de email en Firestore: {} - Existe: {}", email, exists);
            return exists;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error al verificar el email en Firestore: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean saveUserToFirestore(User user) {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", user.getEmail());
        userMap.put("userName", user.getUserName());
        userMap.put("password", user.getPassword());
        userMap.put("isActive", true);
        userMap.put("isConfirmed", false);
        userMap.put("createdAt", FieldValue.serverTimestamp()); //Se anexa fecha de creacion


        ApiFuture<WriteResult> future = firestore.collection("users").document(user.getEmail()).set(userMap);

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
