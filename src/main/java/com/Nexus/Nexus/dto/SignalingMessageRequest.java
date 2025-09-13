package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalingMessageRequest {
    
    @NotBlank(message = "Room ID is required")
    private String roomId;
    
    private String toUserId; // null for broadcast to all
    
    @NotBlank(message = "Message type is required")
    private String type; // offer, answer, ice-candidate, join, leave
    
    @NotNull(message = "Data is required")
    private Object data;
}
