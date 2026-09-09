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

@Service // Marks the class as a service
public class RegisterService {
    // Logger to track events and errors in the current class
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
        logger.info("Attempting to register user: {}", request.getUserName());
        String email = request.getEmail();
        String password = request.getPassword();

        if (!validateEmail(email)) {
            logger.warn("Registration attempt with invalid email");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid email address");
        }

        try{
            //Create the request for a new user of Firebase
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest().
                    setEmail(email).setPassword(password).setDisplayName(request.getUserName()).
                    setEmailVerified(false); //The user needs to verify his email

            //Calling of firebase to create the user (sdk firebase object)
            UserRecord userRecord = firebaseAuth.createUser(createRequest);
            String uid = userRecord.getUid();
            logger.info("User successfully created in Firebase Auth with UID: {}",uid);

            //Saving aditional information of the user in firestore
            User userProfile = new User(uid, email, request.getUserName());
            firestore.collection("users").document(uid).set(userProfile);
            logger.info("User profile saved in Firestore for UID: {}", uid);

            //Send confirmation email
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(uid, "confirmation");
            confirmationEmailService.sendConfirmationEmailAsync(uid,email, tokenInfo.getRawToken(), request.getUserName())
                    .exceptionally(throwable -> {
                        logger.error("Error sending confirmation email to {}: {}", uid, throwable.getMessage());
                        return null;
                    });

            logger.info("Registration process for {} completed.", uid);

        } catch (IllegalArgumentException e) {
            // If the password is invalid before calling firestore
            logger.error("Firebase SDK validation error: {}", e.getMessage());
            String publicMessage = "The password is invalid. It must have at least 8 characters.";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, publicMessage);

        } catch (FirebaseAuthException e) {
            logger.error("Firebase error registering {}:", request.getUserName(), e);

            String publicMessage;
            HttpStatus status;

            AuthErrorCode errorCode = e.getAuthErrorCode();

            if (errorCode != null) {
                // Handle SDK  exceptions
                switch (errorCode) {
                    case EMAIL_ALREADY_EXISTS:
                        publicMessage = "This email is already registered.";
                        status = HttpStatus.CONFLICT;
                        break;
                    default:
                        publicMessage = "Internal error registering the user.";
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                        break;
                }
            } else {
              //Firebase Exceptions, like password politics
                String errorMessage = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();

                if (errorMessage != null && errorMessage.contains("PASSWORD_DOES_NOT_MEET_REQUIREMENTS")) {
                    publicMessage = "The password does not meet the requirements (minimum 8 characters, one uppercase, one lowercase, one number, and one symbol).";
                    status = HttpStatus.BAD_REQUEST;
                } else {
                    publicMessage = "Unknown error registering the user.";
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
