package com.Nexus.Nexus.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "documents")
public class DocumentEntity {
    
    @Id
    private String id;
    
    @NotBlank(message = "Document name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "File path/URL is required")
    private String filePath; // Path to file in cloud storage
    
    @NotBlank(message = "File type is required")
    private String fileType; // MIME type
    
    @NotNull(message = "File size is required")
    private Long fileSize; // Size in bytes
    
    @NotBlank(message = "Owner is required")
    private String ownerId; // User ID who uploaded the document
    
    @NotNull(message = "Document status is required")
    private DocumentStatus status; // DRAFT, REVIEWED, SIGNED
    
    private String version; // Document version
    
    // Meeting association
    private String meetingId; // Associated meeting ID (optional)
    
    // Access permissions
    private List<String> viewerIds; // User IDs who can view the document
    private List<String> editorIds; // User IDs who can edit the document
    
    // E-signature related fields
    private List<DocumentSignature> signatures;
    private boolean requiresSignature;
    private List<String> signatoryIds; // User IDs who need to sign
    
    // Metadata
    private Map<String, Object> metadata; // Additional metadata
    private String thumbnailUrl; // Thumbnail/preview URL
    
    // Audit trail
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastAccessedAt;
    private String lastAccessedBy;
    
    // Version control
    private String parentDocumentId; // For document versions
    private List<String> childVersionIds; // Child versions of this document
    
    public DocumentEntity(String name, String description, String filePath, 
                         String fileType, Long fileSize, String ownerId) {
        this.name = name;
        this.description = description;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.ownerId = ownerId;
        this.status = DocumentStatus.DRAFT;
        this.version = "1.0";
        this.requiresSignature = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
