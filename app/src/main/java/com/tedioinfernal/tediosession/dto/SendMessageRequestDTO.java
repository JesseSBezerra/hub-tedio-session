package com.tedioinfernal.tediosession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequestDTO {
    private String number;
    private String message;
    private Long evolutionInstanceId;
}
