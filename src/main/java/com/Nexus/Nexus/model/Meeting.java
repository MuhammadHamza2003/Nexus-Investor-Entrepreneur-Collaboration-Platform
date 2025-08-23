package com.Nexus.Nexus.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "meetings")
public class Meeting {
    
    @Id
    private String id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Meeting must be scheduled for future")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotBlank(message = "Organizer is required")
    private String organizerId; // User ID who created the meeting
    
    @NotNull(message = "At least one participant is required")
    private List<String> participantIds; // List of User IDs
    
    private MeetingStatus status;
    
    private String meetingLink; // For virtual meetings
    private String location; // For physical meetings
    
    private String agenda;
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor for creating new meetings
    public Meeting(String title, String description, LocalDateTime startTime, 
                  LocalDateTime endTime, String organizerId, List<String> participantIds) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizerId = organizerId;
        this.participantIds = participantIds;
        this.status = MeetingStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
