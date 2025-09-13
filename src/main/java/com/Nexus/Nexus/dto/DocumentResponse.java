package com.Nexus.Nexus.dto;

import com.Nexus.Nexus.model.DocumentStatus;
import com.Nexus.Nexus.model.DocumentSignature;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    
    private String id;
    private String name;
    private String description;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String ownerId;
    private String ownerName; // Populated from User details
    private DocumentStatus status;
    private String version;
    private String meetingId;
    private String meetingTitle; // Populated from Meeting details
    private List<String> viewerIds;
    private List<String> editorIds;
    private List<DocumentSignature> signatures;
    private boolean requiresSignature;
    private List<String> signatoryIds;
    private Map<String, Object> metadata;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastAccessedAt;
    private String lastAccessedBy;
    private String parentDocumentId;
    private List<String> childVersionIds;
    
    // Computed fields
    private boolean canEdit;
    private boolean canView;
    private boolean requiresMySignature;
    private boolean signedByMe;
}
