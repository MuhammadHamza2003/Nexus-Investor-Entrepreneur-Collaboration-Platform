package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.UserResponse;
import com.Nexus.Nexus.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investor")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('INVESTOR')")
public class InvestorController {
    
    @Autowired
    private AuthService authService;
    
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
                    .body(new AuthController.MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/opportunities")
    public ResponseEntity<?> getInvestmentOpportunities() {
        return ResponseEntity.ok(new AuthController.MessageResponse(
            "Investment opportunities endpoint - To be implemented with business logic"
        ));
    }
    
    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio() {
        return ResponseEntity.ok(new AuthController.MessageResponse(
            "Portfolio management endpoint - To be implemented with business logic"
        ));
    }
    
    // Helper class for dashboard response
    public static class DashboardResponse {
        private String message;
        private UserResponse user;
        private String description;
        
        public DashboardResponse(String message, UserResponse user, String description) {
            this.message = message;
            this.user = user;
            this.description = description;
        }
        
        // Getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public UserResponse getUser() { return user; }
        public void setUser(UserResponse user) { this.user = user; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
