package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    
    @NotBlank(message = "Document name is required")
    private String name;
    
    private String description;
    private String meetingId; // Optional - associate with meeting
    private List<String> viewerIds; // Users who can view
    private List<String> editorIds; // Users who can edit
    private boolean requiresSignature;
    private List<String> signatoryIds; // Users who need to sign
    private Map<String, Object> metadata; // Additional metadata
}
