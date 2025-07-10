package com.notesvault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración general de la aplicación
 * Centraliza beans comunes y configuraciones
 */
@Configuration
public class AppConfig {

    /**
     * Bean para el encoder de contraseñas
     * @return BCryptPasswordEncoder configurado
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean para configuración de logging personalizada
     * @return Logger configurado para la aplicación
     */
    @Bean
    public org.slf4j.Logger appLogger() {
        return org.slf4j.LoggerFactory.getLogger("NotesVault");
    }
} 