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
public class MondayCreateItemResponseDTO {
    private DataWrapper data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataWrapper {
        @JsonProperty("create_item")
        private CreateItem createItem;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateItem {
        private String id;
    }
}
