package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.VideoRoomCreateRequest;
import com.Nexus.Nexus.dto.VideoRoomResponse;
import com.Nexus.Nexus.service.VideoRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/video")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class VideoRoomController {
    
    private final VideoRoomService videoRoomService;
    
    @PostMapping("/rooms")
    public ResponseEntity<?> createVideoRoom(@Valid @RequestBody VideoRoomCreateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName(); // This is the username/email from JWT
            
            log.info("Creating video room for username: {} with request: {}", username, request);
            
            VideoRoomResponse response = videoRoomService.createVideoRoom(username, request);
            
            log.info("Successfully created video room: {}", response.getRoomId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating video room: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("details", "Failed to create video room");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<VideoRoomResponse> joinVideoRoom(@PathVariable String roomId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            VideoRoomResponse response = videoRoomService.joinVideoRoom(username, roomId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<Map<String, String>> leaveVideoRoom(@PathVariable String roomId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            videoRoomService.leaveVideoRoom(username, roomId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully left video room");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @PostMapping("/rooms/{roomId}/end")
    public ResponseEntity<Map<String, String>> endVideoRoom(@PathVariable String roomId) {
        try {
            videoRoomService.endVideoRoom(roomId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Video room ended successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<VideoRoomResponse> getVideoRoom(@PathVariable String roomId) {
        try {
            VideoRoomResponse response = videoRoomService.getVideoRoom(roomId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/rooms/active")
    public ResponseEntity<List<VideoRoomResponse>> getUserActiveRooms() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            List<VideoRoomResponse> response = videoRoomService.getUserActiveRooms(username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
