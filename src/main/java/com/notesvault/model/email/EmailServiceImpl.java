package com.notesvault.model.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import com.notesvault.model.contracts.EmailService;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailServiceImpl implements EmailService{

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendEmailAsync(String to, String subject, String body) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Correo enviado a {}: {}", to, subject);
            return CompletableFuture.completedFuture(null);
        }catch(Exception e) {
            logger.error("Error al enviar el correo a {}: {}", to, e.getMessage());
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Error al enviar el correo", e));
            return future;
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Correo enviado a {}: {}", to, subject);
        }catch(Exception e) {
            logger.error("Error al enviar el correo a {}: {}", to, e.getMessage());
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }

    /**
     * Verifica si el servicio de correo está configurado correctamente
     * @return true si está configurado, false en caso contrario
     */
    public boolean isEmailServiceConfigured() {
        return mailSender != null;
    }
}
