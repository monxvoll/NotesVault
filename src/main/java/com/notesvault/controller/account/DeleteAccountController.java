package com.notesvault.controller.account;

import com.notesvault.model.account.DeletionEmailService;
import com.notesvault.model.account.DeletionService;
import com.notesvault.model.account.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/account")
public class DeleteAccountController {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAccountController.class);
    private final DeletionService deleteService;
    private final TokenService tokenService;
    private final DeletionEmailService deletionEmailService;


    public DeleteAccountController(DeletionService deleteService, TokenService tokenService, DeletionEmailService deletionEmailService) {
        this.deleteService = deleteService;
        this.tokenService = tokenService;
        this.deletionEmailService = deletionEmailService;
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<String> deleteAccount(@RequestParam String uid) {
        logger.info("Account deletion request received for user: {}", uid);
        try {
            deleteService.initiateAccountDeletion(uid);
            logger.info("Deletion email sent successfully to: {}", uid);
            return ResponseEntity.ok("Confirmation email sent successfully");
        }catch (ResponseStatusException e) {
            logger.warn("Error sending confirmation email: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @GetMapping("/delete-confirmation")
    public ResponseEntity<?> deleteConfirmation(@RequestParam("token") String token,
                                                @RequestParam("uid") String uid){
        logger.info("Account deletion confirmation request for user: {}",uid);
        try{
            boolean isDelete = deleteService.confirmAccountDeletion(token, uid);
            if(isDelete){
                logger.info("Account successfully deleted for user: {}",uid);
                return  ResponseEntity.ok("Account successfully deleted");
            }else {
                logger.warn("Error attempting to delete account for user: {} - Invalid or already consumed token",uid);
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error attempting to delete account. The link may be invalid");
            }
        }catch (Exception e){
            logger.error("Error deleting account for user {}: {}",uid,e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error attempting to delete account");
        }
    }

    @PostMapping("/resend-delete-confirmation")
    public ResponseEntity<?> resendConfirmationEmail(@RequestParam("uid") String uid){
        logger.info("Resend confirmation email request for user: {}",uid);
        try{

            if(deleteService.isAccountDeleted(uid)){
                logger.warn("Cannot resend token: account {} is already deleted or inactive", uid);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The account does not exist or is inactive");
            }

            deleteService.initiateAccountDeletion(uid);


            logger.info("Deletion email successfully resent for user: {}", uid);
            return ResponseEntity.ok("Deletion email successfully resent");

        } catch (Exception e) {
            logger.error("Error resending deletion email for user {}: {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error resending deletion email");
        }
    }
}
