package com.Nexus.Nexus.service;

import com.Nexus.Nexus.dto.VideoRoomCreateRequest;
import com.Nexus.Nexus.dto.VideoRoomResponse;
import com.Nexus.Nexus.model.Meeting;
import com.Nexus.Nexus.model.MeetingStatus;
import com.Nexus.Nexus.model.VideoRoom;
import com.Nexus.Nexus.model.User;
import com.Nexus.Nexus.repository.VideoRoomRepository;
import com.Nexus.Nexus.repository.MeetingRepository;
import com.Nexus.Nexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoRoomService {
    
    private final VideoRoomRepository videoRoomRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    
    public VideoRoomResponse createVideoRoom(String username, VideoRoomCreateRequest request) {
        log.info("Creating video room for username: {} with meetingId: {}", username, request.getMeetingId());
        
        // Get the user by username to get the actual user ID
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        String userId = user.getId();
        
        log.info("Found user ID: {} for username: {}", userId, username);
        
        // Get the meeting
        Meeting meeting = meetingRepository.findById(request.getMeetingId())
            .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        log.info("Meeting found. OrganizerId: {}, UserId: {}", meeting.getOrganizerId(), userId);
        
        // Check if user is the organizer
        if (!meeting.getOrganizerId().equals(userId)) {
            log.error("Authorization failed. Meeting organizer: {}, Current user: {}", meeting.getOrganizerId(), userId);
            throw new RuntimeException("Only meeting organizer can create video room");
        }
        
        // Check if video room already exists for this meeting
        Optional<VideoRoom> existingRoom = videoRoomRepository.findByMeetingId(request.getMeetingId());
        if (existingRoom.isPresent()) {
            return convertToResponse(existingRoom.get());
        }
        
        // Generate unique room ID
        String roomId = UUID.randomUUID().toString();
        
        // Create video room
        VideoRoom videoRoom = new VideoRoom(
            roomId,
            meeting.getId(),
            userId, // Use the actual user ID, not username
            meeting.getParticipantIds()
        );
        videoRoom.setMaxParticipants(request.getMaxParticipants());
        videoRoom.setRecording(request.isEnableRecording());
        
        // Update meeting with room ID and status
        meeting.setRoomId(roomId);
        meeting.setStatus(MeetingStatus.WAITING_FOR_HOST);
        meeting.setUpdatedAt(LocalDateTime.now());
        
        videoRoomRepository.save(videoRoom);
        meetingRepository.save(meeting);
        
        log.info("Created video room {} for meeting {}", roomId, meeting.getId());
        
        return convertToResponse(videoRoom);
    }
    
    public VideoRoomResponse joinVideoRoom(String username, String roomId) {
        log.info("User {} attempting to join video room: {}", username, roomId);
        
        // Get the user by username to get the actual user ID
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        String userId = user.getId();
        
        VideoRoom videoRoom = videoRoomRepository.findByRoomId(roomId)
            .orElseThrow(() -> new RuntimeException("Video room not found"));

        Meeting meeting = meetingRepository.findById(videoRoom.getMeetingId())
            .orElseThrow(() -> new RuntimeException("Associated meeting not found"));

        // Check if user is allowed to join
        if (!meeting.getParticipantIds().contains(userId) && !meeting.getOrganizerId().equals(userId)) {
            throw new RuntimeException("User not authorized to join this video room");
        }        // Add user to active participants if not already present
        if (!videoRoom.getActiveParticipants().contains(userId)) {
            videoRoom.getActiveParticipants().add(userId);
        }
        
        // Check if this is the host joining
        if (meeting.getOrganizerId().equals(userId) && !videoRoom.isActive()) {
            videoRoom.setActive(true);
            videoRoom.setStartedAt(LocalDateTime.now());
            meeting.setStatus(MeetingStatus.LIVE);
            meeting.setActualStartTime(LocalDateTime.now());
            meetingRepository.save(meeting);
        }
        
        videoRoomRepository.save(videoRoom);
        
        log.info("User {} joined video room {}", userId, roomId);
        
        return convertToResponse(videoRoom);
    }
    
    public void leaveVideoRoom(String username, String roomId) {
        // Get the user by username to get the actual user ID
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        String userId = user.getId();
        
        VideoRoom videoRoom = videoRoomRepository.findByRoomId(roomId)
            .orElseThrow(() -> new RuntimeException("Video room not found"));
        
        // Remove user from active participants
        videoRoom.getActiveParticipants().remove(userId);
        
        // Only end the meeting if no participants are left in the room
        if (videoRoom.getActiveParticipants().isEmpty()) {
            endVideoRoom(roomId);
            return;
        }
        
        // If the host leaves but there are still participants, keep the meeting live
        // but log this event for tracking purposes
        if (videoRoom.getHostId().equals(userId)) {
            log.info("Host {} left video room {} but meeting continues with {} participants", 
                    userId, roomId, videoRoom.getActiveParticipants().size());
        }
        
        videoRoomRepository.save(videoRoom);
        
        log.info("User {} left video room {}", userId, roomId);
    }
    
    public void endVideoRoom(String roomId) {
        VideoRoom videoRoom = videoRoomRepository.findByRoomId(roomId)
            .orElseThrow(() -> new RuntimeException("Video room not found"));
        
        Meeting meeting = meetingRepository.findById(videoRoom.getMeetingId())
            .orElseThrow(() -> new RuntimeException("Associated meeting not found"));
        
        // Update video room
        videoRoom.setActive(false);
        videoRoom.setEndedAt(LocalDateTime.now());
        videoRoom.getActiveParticipants().clear();
        
        // Update meeting
        meeting.setStatus(MeetingStatus.ENDED);
        meeting.setActualEndTime(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        
        videoRoomRepository.save(videoRoom);
        meetingRepository.save(meeting);
        
        log.info("Ended video room {} and meeting {}", roomId, meeting.getId());
    }
    
    public VideoRoomResponse getVideoRoom(String roomId) {
        VideoRoom videoRoom = videoRoomRepository.findByRoomId(roomId)
            .orElseThrow(() -> new RuntimeException("Video room not found"));
        
        return convertToResponse(videoRoom);
    }
    
    public List<VideoRoomResponse> getUserActiveRooms(String username) {
        // Get the user by username to get the actual user ID
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        String userId = user.getId();
        
        List<VideoRoom> hostRooms = videoRoomRepository.findByHostIdAndIsActiveTrue(userId);
        List<VideoRoom> participantRooms = videoRoomRepository.findByParticipantIdsContainingAndIsActiveTrue(userId);
        
        // Combine host rooms and participant rooms, remove duplicates
        List<VideoRoom> allRooms = hostRooms.stream()
            .collect(Collectors.toList());
        
        // Add participant rooms that are not already in the list
        participantRooms.stream()
            .filter(room -> !allRooms.contains(room))
            .forEach(allRooms::add);
        
        return allRooms.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    private VideoRoomResponse convertToResponse(VideoRoom videoRoom) {
        Meeting meeting = meetingRepository.findById(videoRoom.getMeetingId()).orElse(null);
        User host = userRepository.findById(videoRoom.getHostId()).orElse(null);
        
        VideoRoomResponse response = new VideoRoomResponse();
        response.setRoomId(videoRoom.getRoomId());
        response.setMeetingId(videoRoom.getMeetingId());
        response.setMeetingTitle(meeting != null ? meeting.getTitle() : "Unknown Meeting");
        response.setHostId(videoRoom.getHostId());
        response.setHostName(host != null ? host.getUsername() : "Unknown Host");
        response.setParticipantIds(videoRoom.getParticipantIds());
        response.setActiveParticipants(videoRoom.getActiveParticipants());
        response.setActive(videoRoom.isActive());
        response.setRecording(videoRoom.isRecording());
        response.setMaxParticipants(videoRoom.getMaxParticipants());
        response.setCreatedAt(videoRoom.getCreatedAt());
        response.setStartedAt(videoRoom.getStartedAt());
        response.setEndedAt(videoRoom.getEndedAt());
        
        return response;
    }
}
