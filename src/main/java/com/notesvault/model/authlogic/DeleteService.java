package com.notesvault.model.authlogic;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class DeleteService {
    private static final Logger logger = LoggerFactory.getLogger(DeleteService.class);
    private final FirebaseAuth firebaseAuth;
    private final TokenService tokenService;
    private final DeletionEmailService accountDeletionEmailService;

    public DeleteService(FirebaseAuth firebaseAuth, TokenService tokenService, DeletionEmailService accountDeletionEmailService) {
        this.firebaseAuth = firebaseAuth;
        this.tokenService = tokenService;
        this.accountDeletionEmailService = accountDeletionEmailService;
    }

   public void initiateAccountDeletion(String email){
        logger.info("Solicitud para iniciar eliminacion de cuenta para: {}"+email);

        try{
            firebaseAuth.getUserByEmail(email);

            //Send token
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(email, "confirmation");
            logger.info("Token de confirmación de borrado generado para: {}", email);

            accountDeletionEmailService.sendAccountDeletionAsync(email, tokenInfo.getRawToken(), null)
                    .exceptionally(throwable -> {
                        logger.error("Error al enviar correo de eliminación a {}: {}", email, throwable.getMessage());
                        return null;
                    });

            logger.info("Correo de confirmación de borrado enviado exitosamente a: {}", email);


        } catch (FirebaseAuthException e) {
            logger.error("Error de Firebase al buscar el usuario {}: {}", email, e.getAuthErrorCode());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no fue encontrado.", e);
        }
   }
   
}
