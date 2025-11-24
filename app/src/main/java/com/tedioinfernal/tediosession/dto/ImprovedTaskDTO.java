package com.tedioinfernal.tediosession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImprovedTaskDTO {
    private String titulo;
    private String detalhe;
    private String prazo; // Formato DD/MM/YYYY
}
