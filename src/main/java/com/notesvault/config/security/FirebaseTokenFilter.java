package com.notesvault.config.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Collections;

/**
 * Security filter that intercepts every HTTP request to extract and validate
 * the Firebase JWT token from the "Authorization" header.
 */

// (Generic component that spring will manage) is like the "@Service" ( Component but for the business logic )
@Component
// "OncePerRequestFilter" : execute the code for every request
public class FirebaseTokenFilter extends OncePerRequestFilter {
    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

     // A JWT token is composed of three parts (header, payload, and signature)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Get the header "Authorization"
        String authHeader = request.getHeader("Authorization");

        //Check that it has the correct format ("Bearer <token>")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // delete first 7 characters (bearer) to get the clean token

            try {
                // Verify token with firebase (The Scanner")
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                String uid = decodedToken.getUid();

                // Create internal spring auth
                // The user has this UID and no special roles or credentials for now
                // Create temp user credential
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        uid, null, Collections.emptyList());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Save authentication
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (FirebaseAuthException e) {
                // If the token is false, we don't do anything, the user is still anonymous
                logger.error("Error verificando token de Firebase: {}", e);
            }
        }

        // Continue with chain filter
        filterChain.doFilter(request, response);
    }
}
