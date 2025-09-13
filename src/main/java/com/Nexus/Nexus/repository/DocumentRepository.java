package com.Nexus.Nexus.repository;

import com.Nexus.Nexus.model.DocumentEntity;
import com.Nexus.Nexus.model.DocumentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<DocumentEntity, String> {
    
    // Find documents by owner
    List<DocumentEntity> findByOwnerId(String ownerId);
    
    // Find documents by meeting
    List<DocumentEntity> findByMeetingId(String meetingId);
    
    // Find documents by status
    List<DocumentEntity> findByStatus(DocumentStatus status);
    
    // Find documents by viewer access
    @Query("{'$or': [{'ownerId': ?0}, {'viewerIds': ?0}, {'editorIds': ?0}]}")
    List<DocumentEntity> findByUserAccess(String userId);
    
    // Find documents that require signature from a specific user
    @Query("{'signatoryIds': ?0, 'requiresSignature': true}")
    List<DocumentEntity> findDocumentsRequiringSignature(String userId);
    
    // Find documents created within date range
    List<DocumentEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find documents by file type
    List<DocumentEntity> findByFileType(String fileType);
    
    // Find documents by name (case insensitive)
    @Query("{'name': {'$regex': ?0, '$options': 'i'}}")
    List<DocumentEntity> findByNameContainingIgnoreCase(String name);
    
    // Find latest version of documents
    List<DocumentEntity> findByParentDocumentIdIsNull();
    
    // Find document versions
    List<DocumentEntity> findByParentDocumentId(String parentDocumentId);
    
    // Count documents by owner
    long countByOwnerId(String ownerId);
    
    // Count documents requiring signature
    @Query(value = "{'requiresSignature': true}", count = true)
    long countDocumentsRequiringSignature();
}
