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
@RequestMapping("/api/investor")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('INVESTOR')")
@Tag(name = "Investor", description = "Investor-specific endpoints for investment management")
public class InvestorController {
    
    @Autowired
    private AuthService authService;
    
    @Operation(
        summary = "Get investor dashboard",
        description = "Retrieve investor dashboard with personalized information and overview",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not an investor account")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<?> getInvestorDashboard() {
        try {
            UserResponse userResponse = authService.getCurrentUser();
            return ResponseEntity.ok(new DashboardResponse(
                "Welcome to Investor Dashboard, " + userResponse.getFirstName() + "!",
                userResponse,
                "You have access to investment opportunities and portfolio management."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/opportunities")
    public ResponseEntity<?> getInvestmentOpportunities() {
        return ResponseEntity.ok(new MessageResponse(
            "Investment opportunities endpoint - To be implemented with business logic"
        ));
    }
    
    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio() {
        return ResponseEntity.ok(new MessageResponse(
            "Portfolio management endpoint - To be implemented with business logic"
        ));
    }
    
    @GetMapping("/meetings/entrepreneurs")
    public ResponseEntity<?> getEntrepreneurMeetings() {
        return ResponseEntity.ok(new MessageResponse(
            "Meetings with entrepreneurs - Use /api/meetings endpoint for full functionality"
        ));
    }
    
    @GetMapping("/scheduled-meetings")
    public ResponseEntity<?> getScheduledMeetings() {
        return ResponseEntity.ok(new MessageResponse(
            "Scheduled meetings for investor - Use /api/meetings/upcoming endpoint for full functionality"
        ));
    }
    
}
