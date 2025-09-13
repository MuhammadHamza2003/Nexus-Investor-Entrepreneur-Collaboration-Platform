package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.*;
import com.Nexus.Nexus.service.DocumentService;
import com.Nexus.Nexus.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Controller for document processing APIs
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentController {
    
    private final DocumentService documentService;
    private final FileStorageService fileStorageService;
    
    /**
     * Upload a document
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "meetingId", required = false) String meetingId,
            @RequestParam(value = "requiresSignature", defaultValue = "false") boolean requiresSignature,
            @RequestParam(value = "viewerIds", required = false) List<String> viewerIds,
            @RequestParam(value = "editorIds", required = false) List<String> editorIds,
            @RequestParam(value = "signatoryIds", required = false) List<String> signatoryIds,
            Authentication authentication) {
        
        try {
            String userId = authentication.getName();
            
            DocumentUploadRequest request = new DocumentUploadRequest();
            request.setName(name != null ? name : file.getOriginalFilename());
            request.setDescription(description);
            request.setMeetingId(meetingId);
            request.setRequiresSignature(requiresSignature);
            request.setViewerIds(viewerIds);
            request.setEditorIds(editorIds);
            request.setSignatoryIds(signatoryIds);
            
            DocumentResponse response = documentService.uploadDocument(file, request, userId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Document uploaded successfully",
                "document", response
            ));
            
        } catch (IOException e) {
            log.error("Error uploading document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to upload document: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid document upload request", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during document upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
    
    /**
     * Get document by ID
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocument(@PathVariable String documentId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            DocumentResponse document = documentService.getDocument(documentId, userId);
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            log.warn("Error getting document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
    
    /**
     * Get user's documents
     */
    @GetMapping("/my-documents")
    public ResponseEntity<?> getUserDocuments(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<DocumentResponse> documents = documentService.getUserDocuments(userId);
            return ResponseEntity.ok(Map.of(
                "documents", documents,
                "count", documents.size()
            ));
        } catch (Exception e) {
            log.error("Error getting user documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
    
    /**
     * Get documents for a meeting
     */
    @GetMapping("/meeting/{meetingId}")
    public ResponseEntity<?> getMeetingDocuments(@PathVariable String meetingId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<DocumentResponse> documents = documentService.getMeetingDocuments(meetingId, userId);
            return ResponseEntity.ok(Map.of(
                "documents", documents,
                "count", documents.size(),
                "meetingId", meetingId
            ));
        } catch (RuntimeException e) {
            log.warn("Error getting meeting documents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting meeting documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
    
    /**
     * Sign a document
     */
    @PostMapping("/{documentId}/sign")
    public ResponseEntity<?> signDocument(
            @PathVariable String documentId,
            @Valid @RequestBody DocumentSignRequest request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            DocumentResponse document = documentService.signDocument(documentId, request, userId);
            return ResponseEntity.ok(Map.of(
                "message", "Document signed successfully",
                "document", document
            ));
        } catch (RuntimeException e) {
            log.warn("Error signing document: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error signing document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
    
    /**
     * Get documents requiring signature
     */
    @GetMapping("/requiring-signature")
    public ResponseEntity<?> getDocumentsRequiringSignature(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<DocumentResponse> documents = documentService.getDocumentsRequiringSignature(userId);
            return ResponseEntity.ok(Map.of(
                "documents", documents,
                "count", documents.size()
            ));
        } catch (Exception e) {
            log.error("Error getting documents requiring signature", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
    
    /**
     * Delete document
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            documentService.deleteDocument(documentId, userId);
            return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
        } catch (RuntimeException e) {
            log.warn("Error deleting document: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
    
    /**
     * Download document
     */
    @GetMapping("/download/{encodedPath}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String encodedPath, Authentication authentication) {
        try {
            // Decode the path (replace _ with /)
            String filePath = encodedPath.replace("_", "/");
            
            Path path = fileStorageService.getFilePath(filePath);
            Resource resource = new UrlResource(path.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // Get filename
            String filename = path.getFileName().toString();
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(resource);
                
        } catch (Exception e) {
            log.error("Error downloading document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get document preview/thumbnail
     */
    @GetMapping("/{documentId}/preview")
    public ResponseEntity<?> getDocumentPreview(@PathVariable String documentId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            DocumentResponse document = documentService.getDocument(documentId, userId);
            
            if (document.getThumbnailUrl() != null) {
                return ResponseEntity.ok(Map.of(
                    "thumbnailUrl", document.getThumbnailUrl(),
                    "hasPreview", true
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "hasPreview", false,
                    "message", "No preview available for this document"
                ));
            }
        } catch (RuntimeException e) {
            log.warn("Error getting document preview: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting document preview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
    
    /**
     * Get document metadata
     */
    @GetMapping("/{documentId}/metadata")
    public ResponseEntity<?> getDocumentMetadata(@PathVariable String documentId, Authentication authentication) {
        try {
            String userId = authentication.getName();
            DocumentResponse document = documentService.getDocument(documentId, userId);
            
            Map<String, Object> metadata = Map.of(
                "id", document.getId(),
                "name", document.getName(),
                "fileType", document.getFileType(),
                "fileSize", document.getFileSize(),
                "status", document.getStatus().toString(),
                "version", document.getVersion(),
                "createdAt", document.getCreatedAt().toString(),
                "updatedAt", document.getUpdatedAt().toString(),
                "owner", document.getOwnerName() != null ? document.getOwnerName() : "Unknown"
            );
            
            Map<String, Object> additionalInfo = Map.of(
                "requiresSignature", document.isRequiresSignature(),
                "signatureCount", document.getSignatures() != null ? document.getSignatures().size() : 0,
                "metadata", document.getMetadata() != null ? document.getMetadata() : Map.of()
            );
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.putAll(metadata);
            response.putAll(additionalInfo);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Error getting document metadata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting document metadata", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}
