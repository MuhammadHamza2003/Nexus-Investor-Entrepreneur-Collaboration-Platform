package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.UserResponse;
import com.Nexus.Nexus.dto.DashboardResponse;
import com.Nexus.Nexus.dto.MessageResponse;
import com.Nexus.Nexus.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entrepreneur")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ENTREPRENEUR')")
@Tag(name = "Entrepreneur", description = "Entrepreneur-specific endpoints for startup and funding management")
public class EntrepreneurController {
    
    @Autowired
    private AuthService authService;
    
    @Operation(
        summary = "Get entrepreneur dashboard",
        description = "Retrieve entrepreneur dashboard with personalized information and overview",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not an entrepreneur account")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<?> getEntrepreneurDashboard() {
        try {
            UserResponse userResponse = authService.getCurrentUser();
            return ResponseEntity.ok(new DashboardResponse(
                "Welcome to Entrepreneur Dashboard, " + userResponse.getFirstName() + "!",
                userResponse,
                "You have access to funding opportunities and startup management tools."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/funding")
    public ResponseEntity<?> getFundingOpportunities() {
        return ResponseEntity.ok(new MessageResponse(
            "Funding opportunities endpoint - To be implemented with business logic"
        ));
    }
    
    @GetMapping("/startups")
    public ResponseEntity<?> getStartups() {
        return ResponseEntity.ok(new MessageResponse(
            "Startup management endpoint - To be implemented with business logic"
        ));
    }
    
    @PostMapping("/pitch")
    public ResponseEntity<?> submitPitch() {
        return ResponseEntity.ok(new MessageResponse(
            "Pitch submission endpoint - To be implemented with business logic"
        ));
    }
    
    @GetMapping("/meetings/investors")
    public ResponseEntity<?> getInvestorMeetings() {
        return ResponseEntity.ok(new MessageResponse(
            "Meetings with investors - Use /api/meetings endpoint for full functionality"
        ));
    }
    
    @GetMapping("/scheduled-meetings")
    public ResponseEntity<?> getScheduledMeetings() {
        return ResponseEntity.ok(new MessageResponse(
            "Scheduled meetings for entrepreneur - Use /api/meetings/upcoming endpoint for full functionality"
        ));
    }
    
}
