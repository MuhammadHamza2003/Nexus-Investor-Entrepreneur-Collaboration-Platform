package com.Nexus.Nexus.model;

public enum MeetingStatus {
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    RESCHEDULED,
    WAITING_FOR_HOST, // Video room created, waiting for host to join
    LIVE, // Video call is active
    ENDED // Video call has ended
}
