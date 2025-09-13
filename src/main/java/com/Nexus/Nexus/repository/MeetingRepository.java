package com.Nexus.Nexus.repository;

import com.Nexus.Nexus.model.Meeting;
import com.Nexus.Nexus.model.MeetingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends MongoRepository<Meeting, String> {
    
    // Find meetings by organizer
    List<Meeting> findByOrganizerId(String organizerId);
    
    // Find meetings by participant
    List<Meeting> findByParticipantIdsContaining(String participantId);
    
    // Find meetings by status
    List<Meeting> findByStatus(MeetingStatus status);
    
    // Find meetings for a user (either as organizer or participant)
    @Query("{ $or: [ { 'organizerId': ?0 }, { 'participantIds': { $in: [?0] } } ] }")
    List<Meeting> findByUserInvolved(String userId);
    
    // Find meetings for a user within a date range
    @Query("{ $and: [ " +
           "{ $or: [ { 'organizerId': ?0 }, { 'participantIds': { $in: [?0] } } ] }, " +
           "{ 'startTime': { $gte: ?1 } }, " +
           "{ 'endTime': { $lte: ?2 } } " +
           "] }")
    List<Meeting> findByUserInvolvedAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find conflicting meetings for a user in a time range
    @Query("{ $and: [ " +
           "{ $or: [ { 'organizerId': ?0 }, { 'participantIds': { $in: [?0] } } ] }, " +
           "{ 'status': { $in: ['SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'RESCHEDULED'] } }, " +
           "{ $or: [ " +
           "  { $and: [ { 'startTime': { $lt: ?2 } }, { 'endTime': { $gt: ?1 } } ] }, " +
           "  { $and: [ { 'startTime': { $gte: ?1 } }, { 'startTime': { $lt: ?2 } } ] }, " +
           "  { $and: [ { 'endTime': { $gt: ?1 } }, { 'endTime': { $lte: ?2 } } ] } " +
           "] } " +
           "] }")
    List<Meeting> findConflictingMeetings(String userId, LocalDateTime startTime, LocalDateTime endTime);
    
    // Find meetings scheduled for today
    @Query("{ $and: [ " +
           "{ 'startTime': { $gte: ?0 } }, " +
           "{ 'startTime': { $lt: ?1 } } " +
           "] }")
    List<Meeting> findMeetingsForDay(LocalDateTime startOfDay, LocalDateTime endOfDay);
    
    // Find upcoming meetings for a user (includes ongoing meetings)
    // Upcoming is defined as: future meetings with statuses SCHEDULED/CONFIRMED/RESCHEDULED
    // OR meetings currently IN_PROGRESS regardless of startTime
    @Query("{ $and: [ " +
           "{ $or: [ { 'organizerId': ?0 }, { 'participantIds': { $in: [?0] } } ] }, " +
           "{ $or: [ " +
           "  { $and: [ { 'startTime': { $gte: ?1 } }, { 'status': { $in: ['SCHEDULED', 'CONFIRMED', 'RESCHEDULED'] } } ] }, " +
           "  { 'status': 'IN_PROGRESS' } " +
           "] } " +
           "] }")
    List<Meeting> findUpcomingMeetings(String userId, LocalDateTime currentTime);

    // Search meetings by title (case-insensitive) where the user is organizer or participant
    @Query("{ $and: [ " +
           "{ $or: [ { 'organizerId': ?0 }, { 'participantIds': { $in: [?0] } } ] }, " +
           "{ 'title': { $regex: ?1, $options: 'i' } } " +
           "] }")
    List<Meeting> searchMeetingsByTitle(String userId, String titleQuery);
}
