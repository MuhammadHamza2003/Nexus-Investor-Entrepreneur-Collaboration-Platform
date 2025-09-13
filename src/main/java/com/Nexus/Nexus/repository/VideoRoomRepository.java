package com.Nexus.Nexus.repository;

import com.Nexus.Nexus.model.VideoRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface VideoRoomRepository extends MongoRepository<VideoRoom, String> {
    
    Optional<VideoRoom> findByRoomId(String roomId);
    
    Optional<VideoRoom> findByMeetingId(String meetingId);
    
    List<VideoRoom> findByHostIdAndIsActiveTrue(String hostId);
    
    List<VideoRoom> findByParticipantIdsContainingAndIsActiveTrue(String participantId);
    
    void deleteByMeetingId(String meetingId);
}
