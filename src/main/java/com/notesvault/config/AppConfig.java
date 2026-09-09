package com.notesvault.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * General application configuration
 * Centralizes common beans and configurations
 */
@Configuration
public class AppConfig {

    /**
     * Bean for the password encoder
     * @return configured BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean for custom logging configuration
     * @return configured Logger for the application
     */
    @Bean
    public org.slf4j.Logger appLogger() {
        return org.slf4j.LoggerFactory.getLogger("NotesVault");
    }

    /**
     * Bean for  FirebaseAuth
     * @return Instance of FirebaseAuth
     */
    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
} 