package com.Nexus.Nexus.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Document API Controller
 * Tests the actual HTTP endpoints for document processing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test_nexus",
    "spring.data.mongodb.host=localhost", 
    "spring.data.mongodb.port=27017"
})
public class DocumentApiControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testDocumentProcessingHealthEndpoint() {
        String url = "http://localhost:" + port + "/api/test/document-processing";
        
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertEquals("Milestone 5: Document Processing Chamber", body.get("milestone"));
        assertEquals("✅ IMPLEMENTED", body.get("status"));
        assertNotNull(body.get("features"));
        assertNotNull(body.get("endpoints"));
    }

    @Test
    void testDocumentHealthEndpoint() {
        String url = "http://localhost:" + port + "/api/v1/documents/health";
        
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertEquals("Document Processing API is running", body.get("status"));
        assertEquals("Milestone 5 - Document Processing Chamber", body.get("milestone"));
        assertNotNull(body.get("timestamp"));
    }

    @Test 
    void testSimulateUploadEndpoint() {
        String url = "http://localhost:" + port + "/api/test/simulate-upload";
        
        Map<String, Object> uploadData = Map.of(
            "fileName", "test-document.pdf",
            "fileSize", "2048"
        );
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, uploadData, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertEquals("Document upload simulation successful", body.get("message"));
        assertEquals("test-document.pdf", body.get("fileName"));
        assertEquals("2048", body.get("fileSize"));
        assertEquals("UPLOADED", body.get("status"));
        assertNotNull(body.get("documentId"));
    }

    @Test
    void testSimulateSignatureEndpoint() {
        String url = "http://localhost:" + port + "/api/test/simulate-signature";
        
        Map<String, Object> signData = Map.of(
            "documentId", "doc_123",
            "signatureType", "DIGITAL"
        );
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, signData, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertEquals("E-signature simulation successful", body.get("message"));
        assertEquals("doc_123", body.get("documentId"));
        assertEquals("DIGITAL", body.get("signatureType"));
        assertEquals("SIGNED", body.get("status"));
        assertNotNull(body.get("signedAt"));
    }
}
