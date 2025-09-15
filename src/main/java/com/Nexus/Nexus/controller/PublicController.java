package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.TestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Public", description = "Public endpoints that do not require authentication")
public class PublicController {
    
    @Operation(
        summary = "Test public endpoint",
        description = "Test endpoint to verify API is accessible without authentication"
    )
    @ApiResponse(responseCode = "200", description = "API is working correctly", 
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestResponse.class)))
    @GetMapping("/test")
    public ResponseEntity<?> publicTest() {
        return ResponseEntity.ok(new TestResponse(
            "Public API is working!",
            "This endpoint is accessible without authentication"
        ));
    }
    
    @Operation(
        summary = "Health check endpoint",
        description = "Check if the Nexus backend service is running and healthy"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy", 
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = TestResponse.class)))
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(new TestResponse(
            "Application is running",
            "Nexus backend service is healthy"
        ));
    }
    
}
