package com.notesvault.controller.account;

import com.notesvault.dtos.RegisterRequestDTO;
import com.notesvault.model.account.ConfirmationEmailService;
import com.notesvault.model.account.RegisterService;
import com.notesvault.model.account.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private final RegisterService registerService;
    private final ConfirmationEmailService confirmationEmailService;
    private final TokenService tokenService;

    public RegisterController(RegisterService registerService, ConfirmationEmailService confirmationEmailService, TokenService tokenService) {
        this.registerService = registerService;
        this.confirmationEmailService = confirmationEmailService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequestDTO request) {
        logger.info("Registration request received for user: {} ", request.getUserName());
        try {
            registerService.registerUser(request);
            logger.info("Registration successful for user: {}", request.getUserName());
            return ResponseEntity.status(HttpStatus.CREATED).body("A confirmation email has been sent.");
        } catch (ResponseStatusException e) {
            logger.error("Registration error for user {}: {} - {}", request.getUserName(), e.getStatusCode(), e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            logger.error("Unexpected error in registration for user {}: {}", request.getUserName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmAccount(
        @RequestParam("token") String token,
        @RequestParam("uid") String uid
    ){
        logger.info("Account confirmation request for user: {}", uid);
        try{
            boolean isConfirmed = confirmationEmailService.confirmAccount(token, uid);
            if(isConfirmed){
                logger.info("Account successfully confirmed for user: {}", uid);
                return ResponseEntity.ok("Account successfully confirmed");
            }else{
                logger.warn("Error confirming account for user: {} - Invalid or already consumed token", uid);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error confirming account. The link may be invalid or already used.");
            }
        }catch(Exception e){
            logger.error("Error confirming account for user {}: {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error confirming account");
        }
    }
    
    @PostMapping("/resend-confirmation")
    public ResponseEntity<?> resendConfirmationEmail(@RequestParam("uid") String uid, @RequestParam("email") String email) {
        logger.info("Resend confirmation email request for user: {}", uid);
        try {

            // Verify if the user is already confirmed
            if (confirmationEmailService.isUserConfirmed(uid)) {
                logger.warn("Resend attempt for already confirmed user: {}", uid);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The user is already confirmed");
            }
            
            // Generate new confirmation token
            TokenService.GeneratedTokenInfo tokenInfo = tokenService.generateSecureToken(uid, "confirmation");
            logger.info("New confirmation token generated for user: {}", uid);
            
            // Send confirmation email asynchronously
            confirmationEmailService.sendConfirmationEmailAsync(uid , email, tokenInfo.getRawToken(), null)
                .exceptionally(throwable -> {
                    logger.error("Error resending confirmation email to {}: {}", uid, throwable.getMessage());
                    return null;
                });
            
            logger.info("Confirmation email successfully resent for user: {}", uid);
            return ResponseEntity.ok("Confirmation email successfully resent");
            
        } catch (Exception e) {
            logger.error("Error resending confirmation email for user {}: {}", uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error resending the confirmation email");
        }
    }
}
