package com.notesvault.model.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.notesvault.model.contracts.EmailService;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailRecoveryService {
    private static final Logger logger = LoggerFactory.getLogger(EmailRecoveryService.class);
    
    private final EmailService emailService;

    @Value("${app.recovery.base-url}")
    private String baseUrl;
    
    public EmailRecoveryService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Envía un correo de recuperación de contraseña de forma asíncrona
     * @param toEmail Email del destinatario
     * @param recoveryToken Token de recuperación generado
     * @param userName Nombre del usuario
     * @return CompletableFuture que se completa cuando se envía el email
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendPasswordRecoveryEmailAsync(String toEmail, String recoveryToken, String userName) {
        try {
            String content = buildRecoveryEmailContent(toEmail, recoveryToken, userName);
            return emailService.sendEmailAsync(toEmail, "Recuperación de Contraseña - NotesVault", content)
                .thenRun(() -> logger.info("Correo de recuperación enviado exitosamente a: {}", toEmail))
                .exceptionally(throwable -> {
                    logger.error("Error al enviar correo de recuperación a {}: {}", toEmail, throwable.getMessage());
                    return null;
                });
        } catch (Exception e) {
            logger.error("Error al preparar correo de recuperación para {}: {}", toEmail, e.getMessage());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Construye el contenido del correo de recuperación
     * @param email Email del usuario
     * @param token Token de recuperación
     * @param userName Nombre del usuario (opcional)
     * @return Contenido del correo
     */
    private String buildRecoveryEmailContent(String email, String token, String userName) {
        String greeting = userName != null ? "Hola " + userName : "Hola";
        String recoveryUrl = baseUrl + "/recovery/reset?token=" + token + "&email=" + email;
        
        return String.format("""
            %s,
            
            Has solicitado recuperar tu contraseña en NotesVault.
            
            Para continuar con el proceso de recuperación, haz clic en el siguiente enlace:
            %s
            
            Este enlace es válido por 24 horas. Si no solicitaste este cambio, puedes ignorar este correo.
            
            Si el enlace no funciona, copia y pega la siguiente URL en tu navegador:
            %s
            
            Atentamente,
            El equipo de NotesVault
            
            ---
            Este es un correo automático, por favor no respondas a este mensaje.
            """, greeting, recoveryUrl, recoveryUrl);
    }

}
