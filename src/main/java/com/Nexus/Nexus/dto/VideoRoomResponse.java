package com.Nexus.Nexus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoRoomResponse {
    
    private String roomId;
    private String meetingId;
    private String meetingTitle;
    private String hostId;
    private String hostName;
    private List<String> participantIds;
    private List<String> activeParticipants;
    private boolean isActive;
    private boolean isRecording;
    private Integer maxParticipants;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
