package com.notesvault.config.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to prevent the FirebaseTokenFilter from running twice.
 *
 * Since the filter is a @Component, Spring Boot registers it automatically.
 * We also register it manually in SecurityConfig. This class disables the
 * automatic registration so it only runs once (controlled by SecurityConfig).
 */

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<FirebaseTokenFilter> registration(FirebaseTokenFilter filter) {
        FilterRegistrationBean<FirebaseTokenFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); //Disables automatic registration on servlets container
        return registration;
    }
}
