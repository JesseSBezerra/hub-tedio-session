package com.tedioinfernal.tediosession.service;

import com.tedioinfernal.tediosession.dto.AuthRequestDTO;
import com.tedioinfernal.tediosession.dto.AuthResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    private static final String AUTH_URL = "http://191.252.195.25:8101/api/auth/login";
    private static final String DEFAULT_EMAIL = "jessebezerra2@hotmail.com.br";
    private static final String DEFAULT_PASSWORD = "5jWbv*?3teidLHp";

    public String getAuthToken() {
        try {
            log.info("Requesting authentication token");
            
            AuthRequestDTO authRequest = AuthRequestDTO.builder()
                    .email(DEFAULT_EMAIL)
                    .password(DEFAULT_PASSWORD)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AuthRequestDTO> request = new HttpEntity<>(authRequest, headers);
            
            ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
                    AUTH_URL, 
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
