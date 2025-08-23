package com.Nexus.Nexus.dto;

import com.Nexus.Nexus.model.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String bio;
    private String portfolio;
    private String preferences;
    private String profilePicture;
    private String phoneNumber;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
