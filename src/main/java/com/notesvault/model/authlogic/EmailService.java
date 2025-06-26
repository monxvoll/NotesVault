package com.notesvault.model.authlogic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:notesvault@example.com}")
    private String fromEmail;
    
    @Value("${app.recovery.base-url:http://localhost:3000}")
    private String baseUrl;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Envía un correo de recuperación de contraseña
     * @param toEmail Email del destinatario
     * @param recoveryToken Token de recuperación generado
     * @param userName Nombre del usuario (opcional)
     */
    public void sendPasswordRecoveryEmail(String toEmail, String recoveryToken, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Recuperación de Contraseña - NotesVault");
            message.setText(buildRecoveryEmailContent(toEmail, recoveryToken, userName));
            
            mailSender.send(message);
            
            logger.info("Correo de recuperación enviado exitosamente a: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Error al enviar correo de recuperación a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error al enviar el correo de recuperación", e);
        }
    }
    
    /**
     * Envía un correo de recuperación de contraseña con nombre de usuario por defecto
     * @param toEmail Email del destinatario
     * @param recoveryToken Token de recuperación generado
     */
    public void sendPasswordRecoveryEmail(String toEmail, String recoveryToken) {
        sendPasswordRecoveryEmail(toEmail, recoveryToken, null);
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
    
    /**
     * Verifica si el servicio de correo está configurado correctamente
     * @return true si está configurado, false en caso contrario
     */
    public boolean isEmailServiceConfigured() {
        return mailSender != null;
    }
}
