package com.notesvault.model.auth;

import com.notesvault.dtos.LoginRequestDTO;
import com.notesvault.dtos.LoginResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Service in charge of authenticating users against Google Identity Platform
 * using the Web API Key.
 */
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${firebase.api.key}")
    private String apiKey;

    public LoginResponseDTO login(LoginRequestDTO request) {
        // Firebase Auth REST API endpoint to exchange credentials for an ID Token
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

        // Construct Request Body
        Map<String, Object> body = new HashMap<>();
        body.put("email", request.getEmail());
        body.put("password", request.getPassword());
        body.put("returnSecureToken", true);

        // Initialize RestTemplate to perform the external synchronous HTTP POST request
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Do the post
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al comunicar con Firebase");
            }

            // Extract data given by google
            String idToken = (String) response.get("idToken");
            String email = (String) response.get("email");
            String uid = (String) response.get("localId");

            return new LoginResponseDTO(idToken, email, uid);

        } catch (HttpClientErrorException e) {
            logger.error("Error de autenticación en Firebase: {}", e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");

        } catch (Exception e) {
            logger.error("Error inesperado durante el login", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno en login");
        }
    }
}