package com.Nexus.Nexus.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "video_rooms")
public class VideoRoom {
    
    @Id
    private String id;
    
    private String roomId; // Unique room identifier for video calls
    private String meetingId; // Associated meeting ID
    private String hostId; // Meeting organizer
    private List<String> participantIds; // All allowed participants
    private List<String> activeParticipants; // Currently connected participants
    private boolean isActive;
    private boolean isRecording;
    private Integer maxParticipants;
    
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    
    // WebRTC signaling data
    private List<SignalingMessage> signalingHistory;
    
    public VideoRoom(String roomId, String meetingId, String hostId, List<String> participantIds) {
        this.roomId = roomId;
        this.meetingId = meetingId;
        this.hostId = hostId;
        this.participantIds = participantIds != null ? participantIds : new ArrayList<>();
        this.activeParticipants = new ArrayList<>();
        this.isActive = false;
        this.isRecording = false;
        this.maxParticipants = 10;
        this.createdAt = LocalDateTime.now();
        this.signalingHistory = new ArrayList<>();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignalingMessage {
        private String fromUserId;
        private String toUserId;
        private String type; // offer, answer, ice-candidate
        private Object data;
        private LocalDateTime timestamp;
    }
}
