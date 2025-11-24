package com.tedioinfernal.tediosession.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessageDTO {
    
    private String event;
    private String instance;
    private WhatsAppDataDTO data;
    private String destination;
    
    @JsonProperty("date_time")
    private String dateTime;
    
    private String sender;
    
    @JsonProperty("server_url")
    private String serverUrl;
    
    private String apikey;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatsAppDataDTO {
        private KeyDTO key;
        private String pushName;
        private String status;
        private Map<String, Object> message;
        private String messageType;
        private Long messageTimestamp;
        private String instanceId;
        private String source;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyDTO {
        private String remoteJid;
        private Boolean fromMe;
        private String id;
    }
}
