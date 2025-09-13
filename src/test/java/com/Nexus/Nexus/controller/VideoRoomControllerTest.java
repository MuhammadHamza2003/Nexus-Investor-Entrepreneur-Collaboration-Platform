package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.VideoRoomCreateRequest;
import com.Nexus.Nexus.dto.VideoRoomResponse;
import com.Nexus.Nexus.service.VideoRoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class VideoRoomControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private VideoRoomService videoRoomService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(username = "test@example.com")
    public void testCreateVideoRoom() throws Exception {
        VideoRoomCreateRequest request = new VideoRoomCreateRequest();
        request.setMeetingId("meeting123");
        request.setMaxParticipants(10);
        request.setEnableRecording(false);
        
        VideoRoomResponse mockResponse = new VideoRoomResponse();
        mockResponse.setRoomId("room123");
        mockResponse.setMeetingId("meeting123");
        mockResponse.setMeetingTitle("Test Meeting");
        mockResponse.setHostId("test@example.com");
        mockResponse.setHostName("Test User");
        mockResponse.setParticipantIds(Arrays.asList("test@example.com"));
        mockResponse.setActiveParticipants(Arrays.asList());
        mockResponse.setActive(false);
        mockResponse.setRecording(false);
        mockResponse.setMaxParticipants(10);
        mockResponse.setCreatedAt(LocalDateTime.now());
        
        when(videoRoomService.createVideoRoom(anyString(), any(VideoRoomCreateRequest.class)))
                .thenReturn(mockResponse);
        
        mockMvc.perform(post("/api/video/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value("room123"))
                .andExpect(jsonPath("$.meetingId").value("meeting123"));
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    public void testJoinVideoRoom() throws Exception {
        VideoRoomResponse mockResponse = new VideoRoomResponse();
        mockResponse.setRoomId("room123");
        mockResponse.setActive(true);
        mockResponse.setActiveParticipants(Arrays.asList("test@example.com"));
        
        when(videoRoomService.joinVideoRoom(anyString(), anyString()))
                .thenReturn(mockResponse);
        
        mockMvc.perform(post("/api/video/rooms/room123/join"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("room123"))
                .andExpect(jsonPath("$.active").value(true));
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    public void testGetUserActiveRooms() throws Exception {
        VideoRoomResponse mockResponse = new VideoRoomResponse();
        mockResponse.setRoomId("room123");
        mockResponse.setActive(true);
        
        List<VideoRoomResponse> mockRooms = Arrays.asList(mockResponse);
        
        when(videoRoomService.getUserActiveRooms(anyString()))
                .thenReturn(mockRooms);
        
        mockMvc.perform(get("/api/video/rooms/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].roomId").value("room123"));
    }
}
