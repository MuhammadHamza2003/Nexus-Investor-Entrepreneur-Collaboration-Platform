package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSignRequest {
    
    @NotBlank(message = "Signature data is required")
    private String signatureData; // Base64 encoded signature
    
    private String ipAddress;
    private String userAgent;
    private String signatureType; // ELECTRONIC, DIGITAL, HANDWRITTEN
}
