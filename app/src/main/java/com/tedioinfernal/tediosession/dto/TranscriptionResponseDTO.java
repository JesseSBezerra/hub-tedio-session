package com.tedioinfernal.tediosession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResponseDTO {
    private String text;
    private Usage usage;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private String type;
        private Integer totalTokens;
        private Integer inputTokens;
        private Integer outputTokens;
    }
}
