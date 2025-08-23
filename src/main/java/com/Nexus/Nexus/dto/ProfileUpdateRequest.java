package com.Nexus.Nexus.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
public class ProfileUpdateRequest {
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String firstName;
    private String lastName;
    private String bio;
    private String portfolio;
    private String preferences;
    private String profilePicture;
    private String phoneNumber;
    private String location;
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
