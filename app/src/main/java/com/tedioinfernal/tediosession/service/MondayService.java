package com.tedioinfernal.tediosession.service;

import com.tedioinfernal.tediosession.dto.MondayCreateItemResponseDTO;
import com.tedioinfernal.tediosession.dto.MondayCreateUpdateResponseDTO;
import com.tedioinfernal.tediosession.dto.MondayRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    private static final String MONDAY_API_URL = "https://api.monday.com/v2";
    private static final String AUTHORIZATION_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJ0aWQiOjU4OTk0NTIyMiwiYWFpIjoxMSwidWlkIjo5NjYwNDQzNSwiaWFkIjoiMjAyNS0xMS0yNFQxMzozNDo1Mi43NTBaIiwicGVyIjoibWU6d3JpdGUiLCJhY3RpZCI6MzI2MDMxNTYsInJnbiI6InVzZTEifQ.x55cZY4PJhrytILeGGCoH5fo2r_G2m8tc9UiQ0rRGmQ";
    private static final String BOARD_ID = "18387065071";
    private static final String GROUP_ID = "topics";

    public String createTaskItem(String taskName) {
        try {
            log.info("Creating task item in Monday.com: {}", taskName);
            
            // Data atual no formato YYYY-MM-DD
            String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Construir query GraphQL
            String query = String.format(
                "mutation { create_item(board_id: %s, group_id: \"%s\", item_name: \"%s\", column_values: \"{\\\"date\\\":\\\"%s\\\"}\") { id } }",
                BOARD_ID,
                GROUP_ID,
                escapeGraphQL(taskName),
                currentDate
            );
            
            MondayRequestDTO request = MondayRequestDTO.builder()
                    .query(query)
                    .build();
            
            HttpHeaders headers = createHeaders();
            HttpEntity<MondayRequestDTO> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<MondayCreateItemResponseDTO> response = restTemplate.postForEntity(
                    MONDAY_API_URL,
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
                    MONDAY_API_URL,
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
        headers.set("Authorization", AUTHORIZATION_TOKEN);
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
}
