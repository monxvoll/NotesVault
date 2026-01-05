package com.notesvault.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 *This class configures the security filter chain and defines access rules
*/
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseTokenFilter firebaseTokenFilter;

    public SecurityConfig(FirebaseTokenFilter firebaseTokenFilter) {
        this.firebaseTokenFilter = firebaseTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Unnecessary for stateless REST APIs using JWTs)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configure access rules (Authorization)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints: Login, recovery, and documentation (No token required)
                        .requestMatchers(
                                        "/auth/**",
                                        "/recovery/**",
                                        "/account/delete-confirmation", // Allow email confirmation links (no Auth header required)
                                        "/account/resend-delete-confirmation",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                ).permitAll()
                        // Private endpoints: All other requests require a valid token
                        .anyRequest().authenticated()
                )

                // 3. Inject Firebase filter before the standard Spring password authentication filter
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
