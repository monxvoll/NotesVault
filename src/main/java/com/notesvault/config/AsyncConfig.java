package com.notesvault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para operaciones asíncronas en la aplicación
 * Centraliza todos los executors y configuraciones de threads
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * Configuración del pool de threads para el envío asíncrono de emails
     * @return Executor configurado para tareas de email
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Threads mínimos
        executor.setMaxPoolSize(5);  // Threads máximos
        executor.setQueueCapacity(100); // Cola de tareas pendientes
        executor.setThreadNamePrefix("EmailThread-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Configuración del pool de threads para operaciones generales de Firestore
     * @return Executor configurado para tareas de base de datos
     */
    @Bean(name = "firestoreTaskExecutor")
    public Executor firestoreTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // Threads mínimos
        executor.setMaxPoolSize(10); // Threads máximos
        executor.setQueueCapacity(200); // Cola de tareas pendientes
        executor.setThreadNamePrefix("FirestoreThread-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Configuración del pool de threads para operaciones de limpieza y mantenimiento
     * @return Executor configurado para tareas de limpieza
     */
    @Bean(name = "cleanupTaskExecutor")
    public Executor cleanupTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Threads mínimos - 2
        executor.setMaxPoolSize(5);  // Threads máximos - 5
        executor.setQueueCapacity(100); // Cola de tareas pendientes - 100
        executor.setThreadNamePrefix("CleanupThread-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true); // Esperar a que las tareas se completen al cerrar
        executor.setAwaitTerminationSeconds(30); // Esperar hasta 30 segundos
        executor.initialize();
        return executor;
    }
} 