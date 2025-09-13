package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.MeetingCreateRequest;
import com.Nexus.Nexus.dto.MeetingUpdateRequest;
import com.Nexus.Nexus.dto.MeetingResponse;
import com.Nexus.Nexus.service.MeetingService;
import com.Nexus.Nexus.repository.UserRepository;
import com.Nexus.Nexus.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class MeetingController {
    
    private final MeetingService meetingService;
    private final UserRepository userRepository;
    
    @PostMapping
    public ResponseEntity<?> createMeeting(@Valid @RequestBody MeetingCreateRequest request) {
        try {
            String userId = getCurrentUserId();
            MeetingResponse meeting = meetingService.createMeeting(request, userId);
            return ResponseEntity.ok(meeting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Meeting conflict or validation error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to create meeting", e.getMessage()));
        }
    }
    
    @PutMapping("/{meetingId}")
    public ResponseEntity<?> updateMeeting(
            @PathVariable String meetingId,
            @Valid @RequestBody MeetingUpdateRequest request) {
        try {
            String userId = getCurrentUserId();
            MeetingResponse meeting = meetingService.updateMeeting(meetingId, request, userId);
            return ResponseEntity.ok(meeting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Meeting conflict or validation error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse("Access denied", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update meeting", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<?> cancelMeeting(@PathVariable String meetingId) {
        try {
            String userId = getCurrentUserId();
            meetingService.cancelMeeting(meetingId, userId);
            return ResponseEntity.ok(createSuccessResponse("Meeting cancelled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse("Access denied", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to cancel meeting", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getUserMeetings() {
        try {
            String userId = getCurrentUserId();
            List<MeetingResponse> meetings = meetingService.getUserMeetings(userId);
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch meetings", e.getMessage()));
        }
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingMeetings() {
        try {
            String userId = getCurrentUserId();
            List<MeetingResponse> meetings = meetingService.getUpcomingMeetings(userId);
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch upcoming meetings", e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchMeetingsByTitle(@RequestParam("title") String title) {
        try {
            String userId = getCurrentUserId();
            List<MeetingResponse> meetings = meetingService.searchMeetingsByTitle(userId, title);
            return ResponseEntity.ok(meetings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to search meetings", e.getMessage()));
        }
    }
    
    @GetMapping("/{meetingId}")
    public ResponseEntity<?> getMeetingById(@PathVariable String meetingId) {
        try {
            String userId = getCurrentUserId();
            MeetingResponse meeting = meetingService.getMeetingById(meetingId, userId);
            return ResponseEntity.ok(meeting);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse("Access denied", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch meeting", e.getMessage()));
        }
    }
    
    @PutMapping("/{meetingId}/confirm")
    public ResponseEntity<?> confirmMeeting(@PathVariable String meetingId) {
        try {
            String userId = getCurrentUserId();
            meetingService.confirmMeeting(meetingId, userId);
            return ResponseEntity.ok(createSuccessResponse("Meeting confirmed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse("Access denied", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to confirm meeting", e.getMessage()));
        }
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // This returns the username
        
        // Convert username to actual MongoDB user ID
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        return user.getId();
    }
    
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
