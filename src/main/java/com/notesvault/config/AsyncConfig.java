package com.notesvault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous operations in the application
 * Centralizes all executors and thread configurations
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * Thread pool configuration for asynchronous email sending
     * @return configured Executor for email tasks
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Minimum threads
        executor.setMaxPoolSize(5);  // Maximum threads
        executor.setQueueCapacity(100); // Pending tasks queue
        executor.setThreadNamePrefix("EmailThread-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Thread pool configuration for general Firestore operations
     * @return configured Executor for database tasks
     */
    @Bean(name = "firestoreTaskExecutor")
    public Executor firestoreTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // Minimum threads
        executor.setMaxPoolSize(10); // Maximum threads
        executor.setQueueCapacity(200); // Pending tasks queue
        executor.setThreadNamePrefix("FirestoreThread-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Thread pool configuration for cleanup and maintenance operations
     * @return configured Executor for cleanup tasks
     */
    @Bean(name = "cleanupTaskExecutor")
    public Executor cleanupTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Minimum threads - 2
        executor.setMaxPoolSize(5);  // Maximum threads - 5
        executor.setQueueCapacity(100); // Pending tasks queue - 100
        executor.setThreadNamePrefix("CleanupThread-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true); // Wait for tasks to complete on shutdown
        executor.setAwaitTerminationSeconds(30); // Wait up to 30 seconds
        executor.initialize();
        return executor;
    }
} 