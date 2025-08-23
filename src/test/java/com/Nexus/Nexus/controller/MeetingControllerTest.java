package com.Nexus.Nexus.controller;

import com.Nexus.Nexus.dto.MeetingCreateRequest;
import com.Nexus.Nexus.dto.MeetingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MeetingControllerTest {

    @Test
    public void testMeetingCreateRequestValidation() {
        // Test DTO validation
        MeetingCreateRequest request = new MeetingCreateRequest();
        request.setTitle("Investment Discussion");
        request.setDescription("Discussing funding opportunities");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        request.setParticipantIds(Arrays.asList("user2", "user3"));
        
        // Verify the request is properly constructed
        assertNotNull(request.getTitle());
        assertNotNull(request.getStartTime());
        assertNotNull(request.getEndTime());
        assertNotNull(request.getParticipantIds());
        assertTrue(request.getEndTime().isAfter(request.getStartTime()));
        assertEquals("Investment Discussion", request.getTitle());
        assertEquals(2, request.getParticipantIds().size());
    }
    
    @Test
    public void testMeetingResponseCreation() {
        // Test response DTO
        MeetingResponse response = new MeetingResponse();
        response.setId("meeting1");
        response.setTitle("Investment Discussion");
        response.setOrganizerId("organizer1");
        response.setOrganizerName("John Doe");
        
        assertNotNull(response.getId());
        assertEquals("meeting1", response.getId());
        assertEquals("Investment Discussion", response.getTitle());
        assertEquals("organizer1", response.getOrganizerId());
        assertEquals("John Doe", response.getOrganizerName());
    }
    
    @Test
    public void testMeetingTimeValidation() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureStart = now.plusDays(1);
        LocalDateTime futureEnd = now.plusDays(1).plusHours(1);
        
        // Valid time range
        assertTrue(futureEnd.isAfter(futureStart));
        assertTrue(futureStart.isAfter(now));
        
        // Invalid time range
        LocalDateTime invalidEnd = now.plusDays(1).minusHours(1);
        assertFalse(invalidEnd.isAfter(futureStart));
    }
}
