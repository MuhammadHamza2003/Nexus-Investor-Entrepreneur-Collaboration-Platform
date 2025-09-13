package com.Nexus.Nexus.service;

import com.Nexus.Nexus.dto.MeetingCreateRequest;
import com.Nexus.Nexus.dto.MeetingUpdateRequest;
import com.Nexus.Nexus.dto.MeetingResponse;
import com.Nexus.Nexus.model.Meeting;
import com.Nexus.Nexus.model.MeetingStatus;
import com.Nexus.Nexus.repository.MeetingRepository;
import com.Nexus.Nexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MeetingService {
    
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    
    public MeetingResponse createMeeting(MeetingCreateRequest request, String organizerId) {
        // Validate meeting time
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().isEqual(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        // Check for conflicts
        List<String> allParticipants = new ArrayList<>(request.getParticipantIds());
        if (!allParticipants.contains(organizerId)) {
            allParticipants.add(organizerId);
        }
        
        for (String participantId : allParticipants) {
            List<Meeting> conflicts = meetingRepository.findConflictingMeetings(
                participantId, request.getStartTime(), request.getEndTime());
            
            if (!conflicts.isEmpty()) {
                String participantName = getUserName(participantId);
                throw new IllegalArgumentException(
                    "Meeting conflict detected for " + participantName + 
                    " at the requested time slot");
            }
        }
        
        // Validate that all participants exist
        validateParticipants(request.getParticipantIds());
        
        // Ensure organizer is included in participants list (if not already there)
        List<String> participantIds = new ArrayList<>(request.getParticipantIds());
        if (!participantIds.contains(organizerId)) {
            participantIds.add(organizerId);
        }
        
        // Create meeting
        Meeting meeting = new Meeting(
            request.getTitle(),
            request.getDescription(),
            request.getStartTime(),
            request.getEndTime(),
            organizerId,
            participantIds  // Use the updated list that includes the organizer
        );
        
        meeting.setMeetingLink(request.getMeetingLink());
        meeting.setLocation(request.getLocation());
        meeting.setAgenda(request.getAgenda());
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        return convertToResponse(savedMeeting);
    }
    
    public MeetingResponse updateMeeting(String meetingId, MeetingUpdateRequest request, String userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        // Check if user is organizer
        if (!meeting.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Only the organizer can update the meeting");
        }
        
        // If time is being updated, check for conflicts
        LocalDateTime newStartTime = request.getStartTime() != null ? request.getStartTime() : meeting.getStartTime();
        LocalDateTime newEndTime = request.getEndTime() != null ? request.getEndTime() : meeting.getEndTime();
        
        if (request.getStartTime() != null || request.getEndTime() != null) {
            if (newEndTime.isBefore(newStartTime) || newEndTime.isEqual(newStartTime)) {
                throw new IllegalArgumentException("End time must be after start time");
            }
            
            // Check conflicts for all participants (excluding current meeting)
            List<String> allParticipants = request.getParticipantIds() != null ? 
                new ArrayList<>(request.getParticipantIds()) : new ArrayList<>(meeting.getParticipantIds());
            if (!allParticipants.contains(meeting.getOrganizerId())) {
                allParticipants.add(meeting.getOrganizerId());
            }
            
            for (String participantId : allParticipants) {
                List<Meeting> conflicts = meetingRepository.findConflictingMeetings(
                    participantId, newStartTime, newEndTime);
                
                // Remove current meeting from conflicts
                conflicts = conflicts.stream()
                    .filter(m -> !m.getId().equals(meetingId))
                    .collect(Collectors.toList());
                
                if (!conflicts.isEmpty()) {
                    String participantName = getUserName(participantId);
                    throw new IllegalArgumentException(
                        "Meeting conflict detected for " + participantName + 
                        " at the requested time slot");
                }
            }
        }
        
        // Update meeting fields
        if (request.getTitle() != null) {
            meeting.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            meeting.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            meeting.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            meeting.setEndTime(request.getEndTime());
        }
        if (request.getParticipantIds() != null) {
            validateParticipants(request.getParticipantIds());
            List<String> updatedParticipants = new ArrayList<>(request.getParticipantIds());
            if (!updatedParticipants.contains(meeting.getOrganizerId())) {
                updatedParticipants.add(meeting.getOrganizerId());
            }
            meeting.setParticipantIds(updatedParticipants);
        }
        if (request.getMeetingLink() != null) {
            meeting.setMeetingLink(request.getMeetingLink());
        }
        if (request.getLocation() != null) {
            meeting.setLocation(request.getLocation());
        }
        if (request.getAgenda() != null) {
            meeting.setAgenda(request.getAgenda());
        }
        if (request.getNotes() != null) {
            meeting.setNotes(request.getNotes());
        }
        
        meeting.setUpdatedAt(LocalDateTime.now());
        meeting.setStatus(MeetingStatus.RESCHEDULED);
        
        Meeting savedMeeting = meetingRepository.save(meeting);
        return convertToResponse(savedMeeting);
    }
    
    public void cancelMeeting(String meetingId, String userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        // Check if user is organizer
        if (!meeting.getOrganizerId().equals(userId)) {
            throw new RuntimeException("Only the organizer can cancel the meeting");
        }
        
        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setUpdatedAt(LocalDateTime.now());
        meetingRepository.save(meeting);
    }
    
    public List<MeetingResponse> getUserMeetings(String userId) {
        try {
            // Try the optimized query first
            List<Meeting> meetings = meetingRepository.findByUserInvolved(userId);
            
            if (meetings.isEmpty()) {
                // Fallback: get all meetings and filter in Java
                List<Meeting> allMeetings = meetingRepository.findAll();
                meetings = allMeetings.stream()
                        .filter(meeting -> 
                            meeting.getOrganizerId().equals(userId) || 
                            (meeting.getParticipantIds() != null && meeting.getParticipantIds().contains(userId))
                        )
                        .collect(Collectors.toList());
            }
            
            return meetings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.out.println("ERROR in getUserMeetings: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<MeetingResponse> getUpcomingMeetings(String userId) {
        List<Meeting> meetings = meetingRepository.findUpcomingMeetings(userId, LocalDateTime.now());
        // Defensive filter to ensure no cancelled/completed/ended slip through
        return meetings.stream()
            .filter(m -> m.getStatus() != MeetingStatus.CANCELLED
                      && m.getStatus() != MeetingStatus.COMPLETED
                      && m.getStatus() != MeetingStatus.ENDED)
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public MeetingResponse getMeetingById(String meetingId, String userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        // Check if user is involved in the meeting
        if (!meeting.getOrganizerId().equals(userId) && 
            !meeting.getParticipantIds().contains(userId)) {
            throw new RuntimeException("Access denied: You are not involved in this meeting");
        }
        
        return convertToResponse(meeting);
    }

    public List<MeetingResponse> searchMeetingsByTitle(String userId, String titleQuery) {
        if (titleQuery == null || titleQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<Meeting> meetings = meetingRepository.searchMeetingsByTitle(userId, titleQuery.trim());
        return meetings.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public void confirmMeeting(String meetingId, String userId) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new RuntimeException("Meeting not found"));
        
        // Check if user is involved in the meeting
        if (!meeting.getOrganizerId().equals(userId) && 
            !meeting.getParticipantIds().contains(userId)) {
            throw new RuntimeException("Access denied: You are not involved in this meeting");
        }
        
        meeting.setStatus(MeetingStatus.CONFIRMED);
        meeting.setUpdatedAt(LocalDateTime.now());
        meetingRepository.save(meeting);
    }
    
    private void validateParticipants(List<String> participantIds) {
        for (String participantId : participantIds) {
            if (!userRepository.existsById(participantId)) {
                throw new RuntimeException("Participant not found: " + participantId);
            }
        }
    }
    
    private String getUserName(String userId) {
        return userRepository.findById(userId)
            .map(user -> user.getFirstName() + " " + user.getLastName())
            .orElse("Unknown User");
    }
    
    private MeetingResponse convertToResponse(Meeting meeting) {
        MeetingResponse response = new MeetingResponse();
        response.setId(meeting.getId());
        response.setTitle(meeting.getTitle());
        response.setDescription(meeting.getDescription());
        response.setStartTime(meeting.getStartTime());
        response.setEndTime(meeting.getEndTime());
        response.setOrganizerId(meeting.getOrganizerId());
        response.setOrganizerName(getUserName(meeting.getOrganizerId()));
        response.setParticipantIds(meeting.getParticipantIds());
        
        // Get participant names
        List<String> participantNames = meeting.getParticipantIds().stream()
            .map(this::getUserName)
            .collect(Collectors.toList());
        response.setParticipantNames(participantNames);
        
        response.setStatus(meeting.getStatus());
        response.setMeetingLink(meeting.getMeetingLink());
        response.setLocation(meeting.getLocation());
        response.setAgenda(meeting.getAgenda());
        response.setNotes(meeting.getNotes());
        response.setCreatedAt(meeting.getCreatedAt());
        response.setUpdatedAt(meeting.getUpdatedAt());
        
        return response;
    }
}
