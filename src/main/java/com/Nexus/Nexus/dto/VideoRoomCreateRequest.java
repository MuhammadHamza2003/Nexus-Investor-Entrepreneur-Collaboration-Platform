package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoRoomCreateRequest {
    
    @NotBlank(message = "Meeting ID is required")
    private String meetingId;
    
    private Integer maxParticipants = 10;
    private boolean enableRecording = false;
}
