package com.Nexus.Nexus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    // OTP is optional for send-otp endpoint, required for verify-otp endpoint
    private String otp;
    
    private String purpose = "LOGIN"; // LOGIN, REGISTER, PASSWORD_RESET
}