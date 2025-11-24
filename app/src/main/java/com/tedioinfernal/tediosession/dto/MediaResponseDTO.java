package com.tedioinfernal.tediosession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponseDTO {
    private String mediaType;
    private String fileName;
    private String fileLength;
    private String mimetype;
    private String base64;
}
