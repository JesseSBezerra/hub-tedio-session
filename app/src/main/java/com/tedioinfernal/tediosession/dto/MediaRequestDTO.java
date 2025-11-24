package com.tedioinfernal.tediosession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaRequestDTO {
    private String messageId;
    private Long evolutionInstanceId;
    private Boolean convertToMp4;
}
