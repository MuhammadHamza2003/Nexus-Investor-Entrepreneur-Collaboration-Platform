package com.Nexus.Nexus.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class ProfileUpdateRequest {
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "First name can only contain letters and spaces")
    private String firstName;
    
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") 
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Last name can only contain letters and spaces")
    private String lastName;
    
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;
    
    @Size(max = 255, message = "Portfolio URL must not exceed 255 characters")
    @Pattern(regexp = "^(https?://.+)?$", message = "Portfolio must be a valid URL starting with http:// or https://")
    private String portfolio;
    
    @Size(max = 500, message = "Preferences must not exceed 500 characters")
    private String preferences;
    
    @Size(max = 255, message = "Profile picture URL must not exceed 255 characters")
    private String profilePicture;
    
    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Phone number must be a valid international format")
    private String phoneNumber;
    
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$", 
             message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character")
    private String newPassword;
}
