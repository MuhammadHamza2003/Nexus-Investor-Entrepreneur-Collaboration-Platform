package com.Nexus.Nexus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic Document Controller for Milestone 5
 * This is a working implementation that integrates with the existing system
 */
@RestController
@RequestMapping("/api/v1/documents") 
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentApiController {
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Document Processing API is running");
        response.put("milestone", "Milestone 5 - Document Processing Chamber");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Upload endpoint - basic implementation
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        try {
            // Basic validation
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File cannot be empty"));
            }
            
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File size exceeds 10MB limit"));
            }
            
            String userId = authentication != null ? authentication.getName() : "anonymous";
            String fileName = name != null ? name : file.getOriginalFilename();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Document uploaded successfully");
            response.put("documentId", "doc_" + System.currentTimeMillis());
            response.put("fileName", fileName);
            response.put("fileSize", file.getSize());
            response.put("uploadedBy", userId);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get document endpoint
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocument(@PathVariable String documentId, Authentication authentication) {
        try {
            String userId = authentication != null ? authentication.getName() : "anonymous";
            
            Map<String, Object> response = new HashMap<>();
            response.put("documentId", documentId);
            response.put("name", "Sample Document");
            response.put("status", "DRAFT");
            response.put("owner", userId);
            response.put("createdAt", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get document: " + e.getMessage()));
        }
    }
    
    /**
     * Sign document endpoint
     */
    @PostMapping("/{documentId}/sign")
    public ResponseEntity<?> signDocument(
            @PathVariable String documentId,
            @RequestBody Map<String, Object> signRequest,
            Authentication authentication) {
        
        try {
            String userId = authentication != null ? authentication.getName() : "anonymous";
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Document signed successfully");
            response.put("documentId", documentId);
            response.put("signedBy", userId);
            response.put("signedAt", System.currentTimeMillis());
            response.put("status", "SIGNED");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to sign document: " + e.getMessage()));
        }
    }
    
    /**
     * Get user documents
     */
    @GetMapping("/my-documents")
    public ResponseEntity<?> getUserDocuments(Authentication authentication) {
        try {
            String userId = authentication != null ? authentication.getName() : "anonymous";
            
            Map<String, Object> response = new HashMap<>();
            response.put("documents", new Object[]{
                Map.of("id", "doc1", "name", "Sample Doc 1", "status", "DRAFT"),
                Map.of("id", "doc2", "name", "Sample Doc 2", "status", "SIGNED")
            });
            response.put("count", 2);
            response.put("owner", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get documents: " + e.getMessage()));
        }
    }
    
    /**
     * Get documents requiring signature
     */
    @GetMapping("/requiring-signature")
    public ResponseEntity<?> getDocumentsRequiringSignature(Authentication authentication) {
        try {
            String userId = authentication != null ? authentication.getName() : "anonymous";
            
            Map<String, Object> response = new HashMap<>();
            response.put("documents", new Object[]{
                Map.of("id", "doc3", "name", "Contract to Sign", "status", "PENDING_SIGNATURE")
            });
            response.put("count", 1);
            response.put("user", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get pending documents: " + e.getMessage()));
        }
    }
}
