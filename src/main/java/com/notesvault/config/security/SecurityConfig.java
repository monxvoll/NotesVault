package com.notesvault.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                //Enable CORS
                .cors(Customizer.withDefaults())

                // 2. Disable CSRF (Unnecessary for stateless REST APIs using JWTs)
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Configure access rules (Authorization)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints: Login, recovery, and documentation (No token required)
                        .requestMatchers(
                                        "/auth/**",
                                        "/recovery/**",
                                        "/account/delete-confirmation", // Allow email confirmation links (no Auth header required)
                                        "/account/resend-delete-confirmation",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/error" // added to see possible exceptions instead of 403
                                ).permitAll()
                        // Private endpoints: All other requests require a valid token
                        .anyRequest().authenticated()
                )

                // 3. Inject Firebase filter before the standard Spring password authentication filter
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //CORS to allow request from Swagger UI or others origins
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
