package com.notesvault.model.contracts;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    CompletableFuture<Void> sendEmailAsync(String to, String subject, String body);
}
