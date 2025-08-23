package com.Nexus.Nexus.dto;

import com.Nexus.Nexus.model.MeetingStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {
    
    private String id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String organizerId;
    private String organizerName;
    private List<String> participantIds;
    private List<String> participantNames;
    private MeetingStatus status;
    private String meetingLink;
    private String location;
    private String agenda;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
