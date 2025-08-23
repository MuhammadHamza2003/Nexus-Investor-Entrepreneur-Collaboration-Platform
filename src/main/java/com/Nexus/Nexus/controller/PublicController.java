package com.Nexus.Nexus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicController {
    
    @GetMapping("/test")
    public ResponseEntity<?> publicTest() {
        return ResponseEntity.ok(new TestResponse(
            "Public API is working!",
            "This endpoint is accessible without authentication"
        ));
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(new TestResponse(
            "Application is running",
            "Nexus backend service is healthy"
        ));
    }
    
    // Helper class for test responses
    public static class TestResponse {
        private String message;
        private String description;
        
        public TestResponse(String message, String description) {
            this.message = message;
            this.description = description;
        }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
