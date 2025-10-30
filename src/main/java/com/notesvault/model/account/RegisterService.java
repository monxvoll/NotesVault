package com.notesvault.model.account;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.notesvault.dtos.RegisterRequestDTO;
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

    public void registerUser(RegisterRequestDTO request)  {
        logger.info("Intentando registrar usuario: {}", request.getUserName());
        String email = request.getEmail();
        String password = request.getPassword();

        if (!validateEmail(email)) {
            logger.warn("Intento de registro con correo inválido");
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
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(uid, "confirmation");
            confirmationEmailService.sendConfirmationEmailAsync(uid,email, tokenInfo.getRawToken(), request.getUserName())
                    .exceptionally(throwable -> {
                        logger.error("Error al enviar correo de confirmación a {}: {}", uid, throwable.getMessage());
                        return null;
                    });

            logger.info("Proceso de registro para {} completado.", uid);

        } catch (IllegalArgumentException e) {
            // If the password is invalid before calling firestore
            logger.error("Error de validación del SDK de Firebase: {}", e.getMessage());
            String publicMessage = "La contraseña es inválida. Debe tener al menos 8 caracteres.";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, publicMessage);

        } catch (FirebaseAuthException e) {
            logger.error("Error de Firebase al registrar a {}:", request.getUserName(), e);

            String publicMessage;
            HttpStatus status;

            AuthErrorCode errorCode = e.getAuthErrorCode();

            if (errorCode != null) {
                // Handle SDK  exceptions
                switch (errorCode) {
                    case EMAIL_ALREADY_EXISTS:
                        publicMessage = "Este email ya se encuentra registrado.";
                        status = HttpStatus.CONFLICT;
                        break;
                    default:
                        publicMessage = "Error interno al registrar el usuario.";
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                        break;
                }
            } else {
              //Firebase Exceptions, like password politics
                String errorMessage = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();

                if (errorMessage != null && errorMessage.contains("PASSWORD_DOES_NOT_MEET_REQUIREMENTS")) {
                    publicMessage = "La contraseña no cumple los requisitos (mínimo 8 caracteres, una mayúscula, una minúscula, un número y un símbolo).";
                    status = HttpStatus.BAD_REQUEST;
                } else {
                    publicMessage = "Error desconocido al registrar el usuario.";
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                }
            }
            throw new ResponseStatusException(status, publicMessage);
        }
    }

    private boolean validateEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

}
