package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUpdateRequest {
    
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private List<String> participantIds;
    private String meetingLink;
    private String location;
    
    @Size(max = 1000, message = "Agenda must not exceed 1000 characters")
    private String agenda;
    
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
}
