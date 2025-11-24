package com.tedioinfernal.tediosession.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tedioinfernal.tediosession.dto.ImprovedTaskDTO;
import com.tedioinfernal.tediosession.dto.OpenAIRequestDTO;
import com.tedioinfernal.tediosession.dto.OpenAIResponseDTO;
import com.tedioinfernal.tediosession.dto.TranscriptionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_TRANSCRIPTION_URL = "https://api.openai.com/v1/audio/transcriptions";
    private static final String API_KEY = "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";
    private static final String CHAT_MODEL = "gpt-4.1";
    private static final String TRANSCRIPTION_MODEL = "gpt-4o-transcribe";
    
    private static final String SYSTEM_PROMPT = "vamos criar uma historia para um card da mondey, abaixo vc ira receber um decritivo e devolver a estoria explicada da melhor forma possivel, sempre entanda que na abortdagem vamos trabalhar em como cliente eu gostaria de ter uma melhor experiencia fazendo...,ah e devolva apenas a resposta, evite qualquer forma de itaracao pois vou pegar sua resposta e ja usar no card, a resposta devera ser devolvida em formato json com 2 campos titulo e detalhe";

    public ImprovedTaskDTO improveTaskDescription(String userDescription) {
        try {
            log.info("Improving task description with OpenAI GPT");
            log.debug("Original description: {}", userDescription);
            
            // Construir mensagens
            OpenAIRequestDTO.Message systemMessage = OpenAIRequestDTO.Message.builder()
                    .role("system")
                    .content(SYSTEM_PROMPT)
                    .build();
            
            OpenAIRequestDTO.Message userMessage = OpenAIRequestDTO.Message.builder()
                    .role("user")
                    .content(userDescription)
                    .build();
            
            // Construir request
            OpenAIRequestDTO request = OpenAIRequestDTO.builder()
                    .model(CHAT_MODEL)
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .build();
            
            HttpHeaders headers = createHeaders();
            HttpEntity<OpenAIRequestDTO> entity = new HttpEntity<>(request, headers);
            
            // Fazer chamada à API
            ResponseEntity<OpenAIResponseDTO> response = restTemplate.postForEntity(
                    OPENAI_CHAT_URL,
                    entity,
                    OpenAIResponseDTO.class
            );
            
            if (response.getBody() != null && 
                response.getBody().getChoices() != null && 
                !response.getBody().getChoices().isEmpty()) {
                
                String content = response.getBody().getChoices().get(0).getMessage().getContent();
                log.info("GPT response received");
                log.debug("GPT content: {}", content);
                
                // Converter JSON do content para ImprovedTaskDTO
                ImprovedTaskDTO improvedTask = objectMapper.readValue(content, ImprovedTaskDTO.class);
                
                log.info("Task improved - Title: {}", improvedTask.getTitulo());
                return improvedTask;
            }
            
            throw new RuntimeException("Failed to improve task description - empty response");
            
        } catch (Exception e) {
            log.error("Error improving task description with OpenAI", e);
            
            // Fallback: retornar descrição original
            log.warn("Using fallback - original description");
            return ImprovedTaskDTO.builder()
                    .titulo("Tarefa do WhatsApp")
                    .detalhe(userDescription)
                    .build();
        }
    }

    public String transcribeAudio(byte[] audioData, String fileName) {
        try {
            log.info("Transcribing audio file: {}", fileName);
            
            // Criar resource do arquivo de áudio
            ByteArrayResource audioResource = new ByteArrayResource(audioData) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
            
            // Construir multipart form data
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", audioResource);
            body.add("model", TRANSCRIPTION_MODEL);
            body.add("response_format", "json");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(API_KEY);
            
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            
            // Fazer chamada à API
            ResponseEntity<TranscriptionResponseDTO> response = restTemplate.postForEntity(
                    OPENAI_TRANSCRIPTION_URL,
                    request,
                    TranscriptionResponseDTO.class
            );
            
            if (response.getBody() != null && response.getBody().getText() != null) {
                String transcription = response.getBody().getText();
                log.info("Audio transcribed successfully. Text length: {}", transcription.length());
                log.debug("Transcription: {}", transcription);
                return transcription;
            }
            
            throw new RuntimeException("Failed to transcribe audio - empty response");
            
        } catch (Exception e) {
            log.error("Error transcribing audio", e);
            throw new RuntimeException("Failed to transcribe audio", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);
        return headers;
    }
}
