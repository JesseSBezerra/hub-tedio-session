package com.tedioinfernal.tediosession.service;

import com.tedioinfernal.tediosession.dto.MondayCreateItemResponseDTO;
import com.tedioinfernal.tediosession.dto.MondayCreateUpdateResponseDTO;
import com.tedioinfernal.tediosession.dto.MondayRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MondayService {

    private final RestTemplate restTemplate;
    
    @Value("${monday.api.url}")
    private String mondayApiUrl;
    
    @Value("${monday.api.token}")
    private String authorizationToken;
    
    @Value("${monday.board.id}")
    private String boardId;
    
    @Value("${monday.group.id}")
    private String groupId;

    public String createTaskItem(String taskName, String deadline) {
        try {
            log.info("Creating task item in Monday.com: {} with deadline: {}", taskName, deadline);
            
            // Converter prazo de DD/MM/YYYY para YYYY-MM-DD (formato Monday.com)
            String mondayDate = convertToMondayFormat(deadline);
            
            // Construir query GraphQL
            String query = String.format(
                "mutation { create_item(board_id: %s, group_id: \"%s\", item_name: \"%s\", column_values: \"{\\\"date\\\":\\\"%s\\\"}\") { id } }",
                boardId,
                groupId,
                escapeGraphQL(taskName),
                mondayDate
            );
            
            MondayRequestDTO request = MondayRequestDTO.builder()
                    .query(query)
                    .build();
            
            HttpHeaders headers = createHeaders();
            HttpEntity<MondayRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<MondayCreateItemResponseDTO> response = restTemplate.postForEntity(
                    mondayApiUrl,
                    entity,
                    MondayCreateItemResponseDTO.class
            );
            
            if (response.getBody() != null && response.getBody().getData() != null) {
                String itemId = response.getBody().getData().getCreateItem().getId();
                log.info("Task item created successfully with ID: {}", itemId);
                return itemId;
            }
            
            throw new RuntimeException("Failed to create task item - empty response");
            
        } catch (Exception e) {
            log.error("Error creating task item in Monday.com", e);
            throw new RuntimeException("Failed to create task item", e);
        }
    }

    public String createTaskUpdate(String itemId, String updateBody) {
        try {
            log.info("Creating update for item {} in Monday.com", itemId);
            
            // Construir query GraphQL
            String query = String.format(
                "mutation { create_update(item_id: %s, body: \"%s\") { id } }",
                itemId,
                escapeGraphQL(updateBody)
            );
            
            MondayRequestDTO request = MondayRequestDTO.builder()
                    .query(query)
                    .build();
            
            HttpHeaders headers = createHeaders();
            HttpEntity<MondayRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<MondayCreateUpdateResponseDTO> response = restTemplate.postForEntity(
                    mondayApiUrl,
                    entity,
                    MondayCreateUpdateResponseDTO.class
            );
            
            if (response.getBody() != null && response.getBody().getData() != null) {
                String updateId = response.getBody().getData().getCreateUpdate().getId();
                log.info("Update created successfully with ID: {}", updateId);
                return updateId;
            }
            
            throw new RuntimeException("Failed to create update - empty response");
            
        } catch (Exception e) {
            log.error("Error creating update in Monday.com", e);
            throw new RuntimeException("Failed to create update", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorizationToken);
        headers.set("User-Agent", "TedioSession/1.0");
        return headers;
    }
    
    private String escapeGraphQL(String text) {
        if (text == null) {
            return "";
        }
        // Escapar aspas duplas e quebras de linha
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
    
    /**
     * Converte data de DD/MM/YYYY para YYYY-MM-DD (formato Monday.com)
     */
    private String convertToMondayFormat(String deadline) {
        try {
            if (deadline == null || deadline.trim().isEmpty()) {
                // Fallback: 1 semana a partir de hoje
                return LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            // Parse DD/MM/YYYY
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(deadline, inputFormatter);
            
            // Format para YYYY-MM-DD
            return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
        } catch (Exception e) {
            log.warn("Failed to parse deadline '{}', using default (1 week from now)", deadline, e);
            return LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }
}
