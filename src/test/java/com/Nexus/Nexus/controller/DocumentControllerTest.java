package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.model.DocumentEntity;
import com.Nexus.Nexus.model.DocumentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple unit tests for Document Processing functionality
 * Compatible with Spring Boot 3.5.4
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class DocumentControllerTest {
    
    private DocumentEntity testDocument;
    
    @BeforeEach
    void setUp() {
        testDocument = new DocumentEntity();
        testDocument.setId("test-doc-id");
        testDocument.setName("Test Document");
        testDocument.setDescription("Test document description");
        testDocument.setFilePath("documents/test-user/test-file.pdf");
        testDocument.setFileType("application/pdf");
        testDocument.setFileSize(1024L);
        testDocument.setOwnerId("test-user");
        testDocument.setStatus(DocumentStatus.DRAFT);
        testDocument.setVersion("1.0");
        testDocument.setCreatedAt(LocalDateTime.now());
        testDocument.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testDocumentEntityCreation() {
        // Test document entity creation
        assertNotNull(testDocument);
        assertEquals("test-doc-id", testDocument.getId());
        assertEquals("Test Document", testDocument.getName());
        assertEquals("application/pdf", testDocument.getFileType());
        assertEquals(DocumentStatus.DRAFT, testDocument.getStatus());
        assertEquals("test-user", testDocument.getOwnerId());
    }
    
    @Test
    void testDocumentStatusEnum() {
        // Test document status enumeration
        assertEquals("Draft", DocumentStatus.DRAFT.getDisplayName());
        assertEquals("Reviewed", DocumentStatus.REVIEWED.getDisplayName());
        assertEquals("Signed", DocumentStatus.SIGNED.getDisplayName());
        assertEquals("Archived", DocumentStatus.ARCHIVED.getDisplayName());
        assertEquals("Deleted", DocumentStatus.DELETED.getDisplayName());
    }
    
    @Test
    void testDocumentValidation() {
        // Test basic document validation
        assertNotNull(testDocument.getName(), "Document name should not be null");
        assertNotNull(testDocument.getFileType(), "File type should not be null");
        assertNotNull(testDocument.getOwnerId(), "Owner ID should not be null");
        assertTrue(testDocument.getFileSize() > 0, "File size should be greater than 0");
    }
    
    @Test
    void testDocumentMetadata() {
        // Test document metadata
        assertEquals("1.0", testDocument.getVersion());
        assertNotNull(testDocument.getCreatedAt());
        assertNotNull(testDocument.getUpdatedAt());
        assertEquals("documents/test-user/test-file.pdf", testDocument.getFilePath());
    }
}
