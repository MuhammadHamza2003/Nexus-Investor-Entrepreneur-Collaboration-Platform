package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.*;
import com.Nexus.Nexus.service.AuthService;
import com.Nexus.Nexus.service.RateLimitService;
import com.Nexus.Nexus.service.TwoFactorAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "User authentication and profile management endpoints")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private RateLimitService rateLimitService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    @Operation(
        summary = "Register a new user",
        description = "Register a new user with role-based access (INVESTOR or ENTREPRENEUR). Returns JWT token upon successful registration."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Registration failed - Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "429", description = "Too many registration attempts",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest, 
                                        HttpServletRequest request) {
        // Check rate limiting
        if (!rateLimitService.isAllowed(request, "login")) {
            long resetTime = rateLimitService.getSecondsUntilReset(request, "login");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many registration attempts. Please try again in " + resetTime + " seconds."));
        }
        
        try {
            AuthResponse response = authService.register(registerRequest);
            response.setMessage("User registered successfully!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @Operation(
        summary = "Authenticate user",
        description = "Authenticate user with username/email and password. Returns JWT token upon successful authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User authenticated successfully", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Authentication failed - Invalid credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "429", description = "Too many login attempts",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                            HttpServletRequest request) {
        // Check rate limiting
        if (!rateLimitService.isAllowed(request, "login")) {
            long resetTime = rateLimitService.getSecondsUntilReset(request, "login");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many login attempts. Please try again in " + resetTime + " seconds."));
        }
        
        try {
            AuthResponse response = authService.login(loginRequest);
            response.setMessage("User logged in successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid username/email or password!"));
        }
    }
    
    @Operation(
        summary = "Get current user profile",
        description = "Retrieve the profile information of the currently authenticated user",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    @GetMapping("/profile")
    @PreAuthorize("hasRole('INVESTOR') or hasRole('ENTREPRENEUR')")
    public ResponseEntity<?> getUserProfile() {
        try {
            UserResponse userResponse = authService.getCurrentUser();
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @Operation(
        summary = "Update user profile",
        description = "Update the profile information of the currently authenticated user. All fields are optional.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully", 
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    @PutMapping("/profile")
    @PreAuthorize("hasRole('INVESTOR') or hasRole('ENTREPRENEUR')")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody ProfileUpdateRequest updateRequest) {
        try {
            UserResponse userResponse = authService.updateProfile(updateRequest);
            return ResponseEntity.ok(userResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOTP(@Valid @RequestBody SendOtpRequest request, 
                                   HttpServletRequest httpRequest) {
        // Check rate limiting
        if (!rateLimitService.isAllowed(httpRequest, "login")) {
            long resetTime = rateLimitService.getSecondsUntilReset(httpRequest, "login");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new TwoFactorResponse(false, "Too many OTP requests. Please try again in " + resetTime + " seconds."));
        }
        
        try {
            // For demo purposes, we'll use email as userId. In production, get actual userId
            String otp = twoFactorAuthService.generateAndSendOTP(request.getEmail(), request.getEmail(), request.getPurpose());
            
            // Return OTP for testing purposes (remove in production)
            return ResponseEntity.ok(new TwoFactorResponse(true, "OTP sent successfully to your email", otp, 5));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(false, "Failed to send OTP: " + e.getMessage()));
        }
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody TwoFactorRequest request) {
        try {
            // Validate that OTP is provided for verification
            if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new TwoFactorResponse(false, "OTP is required for verification"));
            }
            
            boolean isValid = twoFactorAuthService.verifyOTP(request.getEmail(), request.getOtp(), request.getPurpose());
            
            if (isValid) {
                return ResponseEntity.ok(new TwoFactorResponse(true, "OTP verified successfully"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new TwoFactorResponse(false, "Invalid or expired OTP"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new TwoFactorResponse(false, "OTP verification failed: " + e.getMessage()));
        }
    }
    
}
