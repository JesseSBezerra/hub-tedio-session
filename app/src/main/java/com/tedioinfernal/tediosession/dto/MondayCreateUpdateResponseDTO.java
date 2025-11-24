package com.tedioinfernal.tediosession.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MondayCreateUpdateResponseDTO {
    private DataWrapper data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataWrapper {
        @JsonProperty("create_update")
        private CreateUpdate createUpdate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUpdate {
        private String id;
    }
}
