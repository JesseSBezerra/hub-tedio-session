package com.tedioinfernal.tediosession.service;

import com.tedioinfernal.tediosession.dto.MediaRequestDTO;
import com.tedioinfernal.tediosession.dto.MediaResponseDTO;
import com.tedioinfernal.tediosession.dto.SendMessageRequestDTO;
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
public class EvolutionApiService {

    private final RestTemplate restTemplate;
    private final AuthService authService;
    
    private static final String SEND_MESSAGE_URL = "http://191.252.195.25:8101/api/evolution/message";
    private static final String GET_MEDIA_URL = "http://191.252.195.25:8101/api/evolution/media";

    public void sendMessage(String number, String message, Long evolutionInstanceId) {
        try {
            log.info("Sending message to number: {}", number);
            
            // Obter token de autenticação
            String token = authService.getAuthToken();
            
            // Preparar request
            SendMessageRequestDTO messageRequest = SendMessageRequestDTO.builder()
                    .number(number)
                    .message(message)
                    .evolutionInstanceId(evolutionInstanceId)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            HttpEntity<SendMessageRequestDTO> request = new HttpEntity<>(messageRequest, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                    SEND_MESSAGE_URL, 
                    request, 
                    String.class
            );
            
            log.info("Message sent successfully. Response: {}", response.getBody());
            
        } catch (Exception e) {
            log.error("Error sending message to number: {}", number, e);
            throw new RuntimeException("Failed to send message", e);
        }
    }
    
    public void sendMenuMessage(String number, Long evolutionInstanceId) {
        String menuMessage = "Olá! Bem-vindo ao nosso atendimento.\n\n" +
                "Por favor, escolha uma opção:\n\n" +
                "1️⃣ - Detalhar história\n" +
                "2️⃣ - Encerrar sessão\n\n" +
                "Digite o número da opção desejada.";
        
        sendMessage(number, menuMessage, evolutionInstanceId);
    }
    
    public MediaResponseDTO getMedia(String messageId, Long evolutionInstanceId) {
        try {
            log.info("Getting media for messageId: {}", messageId);
            
            // Obter token de autenticação
            String token = authService.getAuthToken();
            
            // Preparar request
            MediaRequestDTO mediaRequest = MediaRequestDTO.builder()
                    .messageId(messageId)
                    .evolutionInstanceId(evolutionInstanceId)
                    .convertToMp4(false)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            HttpEntity<MediaRequestDTO> request = new HttpEntity<>(mediaRequest, headers);
            
            ResponseEntity<MediaResponseDTO> response = restTemplate.postForEntity(
                    GET_MEDIA_URL, 
                    request, 
                    MediaResponseDTO.class
            );
            
            log.info("Media retrieved successfully. Type: {}", response.getBody().getMediaType());
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error getting media for messageId: {}", messageId, e);
            throw new RuntimeException("Failed to get media", e);
        }
    }
}
