package com.Nexus.Nexus.service;

import com.Nexus.Nexus.model.*;
import com.Nexus.Nexus.repository.DocumentRepository;
import com.Nexus.Nexus.repository.UserRepository;
import com.Nexus.Nexus.repository.MeetingRepository;
import com.Nexus.Nexus.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final FileStorageService fileStorageService;
    
    @Value("${app.document.max-file-size:10485760}") // 10MB default
    private long maxFileSize;
    
    @Value("${app.document.allowed-types:application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,text/plain,image/jpeg,image/png}")
    private String allowedFileTypes;
    

    
    /**
     * Upload a document
     */
    public DocumentResponse uploadDocument(MultipartFile file, DocumentUploadRequest request, String userId) 
            throws IOException {
        
        // Validate file
        validateFile(file);
        
        // Get file info
        String detectedType = detectFileType(file);
        String fileName = file.getOriginalFilename();
        
        // Upload to storage
        String filePath = fileStorageService.uploadFile(file, "documents/" + userId);
        String thumbnailUrl = null;
        
        // Generate thumbnail if it's an image or PDF
        if (detectedType.startsWith("image/") || detectedType.equals("application/pdf")) {
            try {
                thumbnailUrl = fileStorageService.generateThumbnail(file, filePath);
            } catch (Exception e) {
                log.warn("Could not generate thumbnail for file: " + fileName, e);
            }
        }
        
        // Create document entity
        DocumentEntity document = new DocumentEntity(
            request.getName() != null ? request.getName() : fileName,
            request.getDescription(),
            filePath,
            detectedType,
            file.getSize(),
            userId
        );
        
        document.setMeetingId(request.getMeetingId());
        document.setViewerIds(request.getViewerIds() != null ? request.getViewerIds() : new ArrayList<>());
        document.setEditorIds(request.getEditorIds() != null ? request.getEditorIds() : new ArrayList<>());
        document.setRequiresSignature(request.isRequiresSignature());
        document.setSignatoryIds(request.getSignatoryIds() != null ? request.getSignatoryIds() : new ArrayList<>());
        document.setMetadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>());
        document.setThumbnailUrl(thumbnailUrl);
        document.setSignatures(new ArrayList<>());
        
        // Save document
        DocumentEntity savedDocument = documentRepository.save(document);
        
        log.info("Document uploaded successfully: {} by user: {}", savedDocument.getId(), userId);
        
        return convertToResponse(savedDocument, userId);
    }
    
    /**
     * Get document by ID
     */
    public DocumentResponse getDocument(String documentId, String userId) {
        DocumentEntity document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        // Check access permissions
        if (!hasViewAccess(document, userId)) {
            throw new RuntimeException("Access denied to document: " + documentId);
        }
        
        // Update last accessed
        document.setLastAccessedAt(LocalDateTime.now());
        document.setLastAccessedBy(userId);
        documentRepository.save(document);
        
        return convertToResponse(document, userId);
    }
    
    /**
     * Get documents for a user
     */
    public List<DocumentResponse> getUserDocuments(String userId) {
        List<DocumentEntity> documents = documentRepository.findByUserAccess(userId);
        return documents.stream()
            .map(doc -> convertToResponse(doc, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Get documents for a meeting
     */
    public List<DocumentResponse> getMeetingDocuments(String meetingId, String userId) {
        // Verify user has access to the meeting
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("Meeting not found: " + meetingId));
        
        if (!meeting.getOrganizerId().equals(userId) && 
            !meeting.getParticipantIds().contains(userId)) {
            throw new RuntimeException("Access denied to meeting documents");
        }
        
        List<DocumentEntity> documents = documentRepository.findByMeetingId(meetingId);
        return documents.stream()
            .filter(doc -> hasViewAccess(doc, userId))
            .map(doc -> convertToResponse(doc, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Sign a document
     */
    public DocumentResponse signDocument(String documentId, DocumentSignRequest request, String userId) {
        DocumentEntity document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        // Check if document requires signature
        if (!document.isRequiresSignature()) {
            throw new RuntimeException("Document does not require signature");
        }
        
        // Check if user is allowed to sign
        if (!document.getSignatoryIds().contains(userId)) {
            throw new RuntimeException("User not authorized to sign this document");
        }
        
        // Check if already signed
        boolean alreadySigned = document.getSignatures().stream()
            .anyMatch(sig -> sig.getSignerId().equals(userId));
        
        if (alreadySigned) {
            throw new RuntimeException("Document already signed by user");
        }
        
        // Get user details
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        // Create signature
        DocumentSignature signature = new DocumentSignature(
            userId,
            user.getFullName(),
            user.getEmail(),
            request.getSignatureData(),
            SignatureType.valueOf(request.getSignatureType().toUpperCase())
        );
        signature.setIpAddress(request.getIpAddress());
        signature.setUserAgent(request.getUserAgent());
        
        // Add signature to document
        if (document.getSignatures() == null) {
            document.setSignatures(new ArrayList<>());
        }
        document.getSignatures().add(signature);
        
        // Check if all required signatures are collected
        boolean allSigned = document.getSignatoryIds().stream()
            .allMatch(signatoryId -> document.getSignatures().stream()
                .anyMatch(sig -> sig.getSignerId().equals(signatoryId)));
        
        if (allSigned) {
            document.setStatus(DocumentStatus.SIGNED);
        }
        
        document.setUpdatedAt(LocalDateTime.now());
        DocumentEntity savedDocument = documentRepository.save(document);
        
        log.info("Document signed: {} by user: {}", documentId, userId);
        
        return convertToResponse(savedDocument, userId);
    }
    
    /**
     * Get documents requiring signature from user
     */
    public List<DocumentResponse> getDocumentsRequiringSignature(String userId) {
        List<DocumentEntity> documents = documentRepository.findDocumentsRequiringSignature(userId);
        return documents.stream()
            .filter(doc -> !isSignedByUser(doc, userId)) // Not already signed
            .map(doc -> convertToResponse(doc, userId))
            .collect(Collectors.toList());
    }
    
    /**
     * Delete document
     */
    public void deleteDocument(String documentId, String userId) {
        DocumentEntity document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        // Only owner can delete
        if (!document.getOwnerId().equals(userId)) {
            throw new RuntimeException("Only document owner can delete");
        }
        
        // Delete from storage
        try {
            fileStorageService.deleteFile(document.getFilePath());
            if (document.getThumbnailUrl() != null) {
                fileStorageService.deleteFile(document.getThumbnailUrl());
            }
        } catch (Exception e) {
            log.warn("Could not delete file from storage: " + document.getFilePath(), e);
        }
        
        // Delete from database
        documentRepository.deleteById(documentId);
        
        log.info("Document deleted: {} by user: {}", documentId, userId);
    }
    
    /**
     * Get document download URL
     */
    public String getDocumentDownloadUrl(String documentId, String userId) {
        DocumentEntity document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        if (!hasViewAccess(document, userId)) {
            throw new RuntimeException("Access denied to document: " + documentId);
        }
        
        return fileStorageService.generateDownloadUrl(document.getFilePath());
    }
    
    // Helper methods
    
    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        
        String detectedType = detectFileType(file);
        List<String> allowedTypes = Arrays.asList(allowedFileTypes.split(","));
        
        if (!allowedTypes.contains(detectedType)) {
            throw new IllegalArgumentException("File type not allowed: " + detectedType);
        }
    }
    
    private boolean hasViewAccess(DocumentEntity document, String userId) {
        return document.getOwnerId().equals(userId) ||
               (document.getViewerIds() != null && document.getViewerIds().contains(userId)) ||
               (document.getEditorIds() != null && document.getEditorIds().contains(userId));
    }
    
    private boolean hasEditAccess(DocumentEntity document, String userId) {
        return document.getOwnerId().equals(userId) ||
               (document.getEditorIds() != null && document.getEditorIds().contains(userId));
    }
    
    private boolean isSignedByUser(DocumentEntity document, String userId) {
        return document.getSignatures() != null &&
               document.getSignatures().stream()
                   .anyMatch(sig -> sig.getSignerId().equals(userId));
    }
    
    private DocumentResponse convertToResponse(DocumentEntity document, String userId) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setName(document.getName());
        response.setDescription(document.getDescription());
        response.setFilePath(document.getFilePath());
        response.setFileType(document.getFileType());
        response.setFileSize(document.getFileSize());
        response.setOwnerId(document.getOwnerId());
        response.setStatus(document.getStatus());
        response.setVersion(document.getVersion());
        response.setMeetingId(document.getMeetingId());
        response.setViewerIds(document.getViewerIds());
        response.setEditorIds(document.getEditorIds());
        response.setSignatures(document.getSignatures());
        response.setRequiresSignature(document.isRequiresSignature());
        response.setSignatoryIds(document.getSignatoryIds());
        response.setMetadata(document.getMetadata());
        response.setThumbnailUrl(document.getThumbnailUrl());
        response.setCreatedAt(document.getCreatedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        response.setLastAccessedAt(document.getLastAccessedAt());
        response.setLastAccessedBy(document.getLastAccessedBy());
        response.setParentDocumentId(document.getParentDocumentId());
        response.setChildVersionIds(document.getChildVersionIds());
        
        // Set computed fields
        response.setCanView(hasViewAccess(document, userId));
        response.setCanEdit(hasEditAccess(document, userId));
        response.setRequiresMySignature(document.getSignatoryIds() != null && 
                                       document.getSignatoryIds().contains(userId));
        response.setSignedByMe(isSignedByUser(document, userId));
        
        // Populate owner name
        try {
            User owner = userRepository.findById(document.getOwnerId()).orElse(null);
            if (owner != null) {
                response.setOwnerName(owner.getFullName());
            }
        } catch (Exception e) {
            log.warn("Could not fetch owner details for document: " + document.getId());
        }
        
        // Populate meeting title
        if (document.getMeetingId() != null) {
            try {
                Meeting meeting = meetingRepository.findById(document.getMeetingId()).orElse(null);
                if (meeting != null) {
                    response.setMeetingTitle(meeting.getTitle());
                }
            } catch (Exception e) {
                log.warn("Could not fetch meeting details for document: " + document.getId());
            }
        }
        
        return response;
    }
    
    /**
     * Simple file type detection based on file extension
     * In production, use Apache Tika for more robust detection
     */
    private String detectFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return "application/octet-stream";
        }
        
        String extension = filename.toLowerCase();
        if (extension.endsWith(".pdf")) {
            return "application/pdf";
        } else if (extension.endsWith(".doc")) {
            return "application/msword";
        } else if (extension.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (extension.endsWith(".txt")) {
            return "text/plain";
        } else if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (extension.endsWith(".png")) {
            return "image/png";
        } else if (extension.endsWith(".gif")) {
            return "image/gif";
        } else {
            return file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        }
    }
}
