package com.tedioinfernal.tediosession.service;

import com.tedioinfernal.tediosession.dto.AuthRequestDTO;
import com.tedioinfernal.tediosession.dto.AuthResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    
    @Value("${evolution.api.url}")
    private String evolutionApiUrl;
    
    @Value("${evolution.auth.email}")
    private String authEmail;
    
    @Value("${evolution.auth.password}")
    private String authPassword;

    public String getAuthToken() {
        try {
            log.info("Requesting authentication token");
            
            AuthRequestDTO authRequest = AuthRequestDTO.builder()
                    .email(authEmail)
                    .password(authPassword)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AuthRequestDTO> request = new HttpEntity<>(authRequest, headers);
            
            ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
                    evolutionApiUrl + "/api/auth/login", 
                    request, 
                    AuthResponseDTO.class
            );
            
            if (response.getBody() != null && response.getBody().getToken() != null) {
                log.info("Authentication token obtained successfully");
                return response.getBody().getToken();
            }
            
            log.error("Failed to obtain authentication token - empty response");
            throw new RuntimeException("Failed to obtain authentication token");
            
        } catch (Exception e) {
            log.error("Error obtaining authentication token", e);
            throw new RuntimeException("Failed to authenticate", e);
        }
    }
}
