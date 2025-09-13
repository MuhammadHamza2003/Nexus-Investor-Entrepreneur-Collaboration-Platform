package com.Nexus.Nexus.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents an electronic signature on a document
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSignature {
    
    private String signerId; // User ID who signed
    private String signerName; // Name of the signer
    private String signerEmail; // Email of the signer
    private LocalDateTime signedAt; // When the document was signed
    private String signatureData; // Base64 encoded signature image or digital signature
    private String ipAddress; // IP address from where signed
    private String userAgent; // Browser/device info
    private SignatureType signatureType; // ELECTRONIC, DIGITAL, HANDWRITTEN
    private String certificateId; // For digital signatures
    
    public DocumentSignature(String signerId, String signerName, String signerEmail, 
                           String signatureData, SignatureType signatureType) {
        this.signerId = signerId;
        this.signerName = signerName;
        this.signerEmail = signerEmail;
        this.signatureData = signatureData;
        this.signatureType = signatureType;
        this.signedAt = LocalDateTime.now();
    }
}


