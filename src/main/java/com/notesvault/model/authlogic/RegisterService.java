package com.notesvault.model.authlogic;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.notesvault.dtos.RegisterRequest;
import com.notesvault.model.entities.User;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service // Marca la clase como un servicio
public class RegisterService {
    //Logger para rastrear eventos y errores en la clase actual
    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);
    private final Firestore firestore;
    private final ConfirmationEmailService confirmationEmailService;
    private final TokenService tokenService;
    private final FirebaseAuth firebaseAuth;

    public RegisterService(Firestore firestore, ConfirmationEmailService confirmationEmailService, TokenService tokenService, FirebaseAuth firebaseAuth) {
        this.firestore = firestore;
        this.confirmationEmailService = confirmationEmailService;
        this.tokenService = tokenService;
        this.firebaseAuth = firebaseAuth;
    }

    public void registerUser(RegisterRequest request)  {
        logger.info("Intentando registrar usuario: {} ({})", request.getUserName(), request.getEmail());
        String email = request.getEmail();
        String password = request.getPassword();

        if (!validateEmail(email)) {
            logger.warn("Intento de registro con correo inválido: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Correo electrónico invalido");
        }

        try{
            //Create the request for a new user of Firebase
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest().
                    setEmail(email).setPassword(password).setDisplayName(request.getUserName()).
                    setEmailVerified(false); //The user needs to verify his email

            //Calling of firebase to create the user (sdk firebase object)
            UserRecord userRecord = firebaseAuth.createUser(createRequest);
            String uid = userRecord.getUid();
            logger.info("Usuario creado exitosamente en Firebase Auth con UID: {}",uid);

            //Saving aditional information of the user in firestore
            User userProfile = new User(uid, email, request.getUserName());
            firestore.collection("users").document(uid).set(userProfile);
            logger.info("Perfil de usuario guardado en Firestore para UID: {}", uid);

            //Send confirmation email
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(email, "confirmation");
            confirmationEmailService.sendConfirmationEmailAsync(email, tokenInfo.getRawToken(), request.getUserName())
                    .exceptionally(throwable -> {
                        logger.error("Error al enviar correo de confirmación a {}: {}", email, throwable.getMessage());
                        return null;
                    });

            logger.info("Proceso de registro para {} completado.", email);

        } catch (FirebaseAuthException e) {
            // Handle Firebase Specific Errors
            logger.error("Error de Firebase al registrar a {}: {}", email, e.getAuthErrorCode(), e);
            String publicMessage;
            HttpStatus status;

            //Get error code
            String errorCode = e.getAuthErrorCode().name();

            if (errorCode.equals("EMAIL_ALREADY_EXISTS")) {
                publicMessage = "Este email ya se encuentra registrado.";
                status = HttpStatus.CONFLICT;

            } else if (errorCode.equals("WEAK_PASSWORD")) {
                publicMessage = "La contraseña es demasiado débil. Debe tener al menos 6 caracteres.";
                status = HttpStatus.BAD_REQUEST;
            } else {
                publicMessage = "Error interno al registrar el usuario.";
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            throw new ResponseStatusException(status, publicMessage);
        }
    }

    public boolean userExists(String email) {
        try {
            // This method return an exception if the email doesnt exist
            firebaseAuth.getUserByEmail(email);
            return true;
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND || e.getAuthErrorCode() == AuthErrorCode.EMAIL_NOT_FOUND) {
                return false;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al verificar el usuario.", e);
        }
    }

    private boolean validateEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

}
