package com.Nexus.Nexus.service;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.Nexus.Nexus.model.VideoRoom;
import com.Nexus.Nexus.repository.VideoRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "socketio.server.enabled", havingValue = "true", matchIfMissing = true)
public class SignalingService {
    
    private final SocketIOServer socketIOServer;
    private final VideoRoomRepository videoRoomRepository;
    private final VideoRoomService videoRoomService;
    
    // Maps to track user sessions and rooms
    private final Map<String, String> userSessions = new ConcurrentHashMap<>(); // sessionId -> userId
    private final Map<String, String> sessionRooms = new ConcurrentHashMap<>(); // sessionId -> roomId
    
    @PostConstruct
    public void startServer() {
        socketIOServer.addConnectListener(onConnected());
        socketIOServer.addDisconnectListener(onDisconnected());
        socketIOServer.addEventListener("join-room", Object.class, onJoinRoom());
        socketIOServer.addEventListener("leave-room", Object.class, onLeaveRoom());
        socketIOServer.addEventListener("webrtc-offer", Object.class, onWebRTCOffer());
        socketIOServer.addEventListener("webrtc-answer", Object.class, onWebRTCAnswer());
        socketIOServer.addEventListener("ice-candidate", Object.class, onIceCandidate());
        
        socketIOServer.start();
        log.info("Socket.IO server started on port: {}", socketIOServer.getConfiguration().getPort());
    }
    
    @PreDestroy
    public void stopServer() {
        if (socketIOServer != null) {
            socketIOServer.stop();
            log.info("Socket.IO server stopped");
        }
    }
    
    private ConnectListener onConnected() {
        return (client) -> {
            String sessionId = client.getSessionId().toString();
            log.info("Client connected: {}", sessionId);
        };
    }
    
    private DisconnectListener onDisconnected() {
        return (client) -> {
            String sessionId = client.getSessionId().toString();
            String userId = userSessions.get(sessionId);
            String roomId = sessionRooms.get(sessionId);
            
            if (userId != null && roomId != null) {
                handleUserLeaveRoom(userId, roomId, sessionId);
            }
            
            userSessions.remove(sessionId);
            sessionRooms.remove(sessionId);
            
            log.info("Client disconnected: {}", sessionId);
        };
    }
    
    private DataListener<Object> onJoinRoom() {
        return (client, data, ackSender) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            String sessionId = client.getSessionId().toString();
            String roomId = (String) dataMap.get("roomId");
            String userId = (String) dataMap.get("userId");
            
            try {
                // Validate room exists
                videoRoomRepository.findByRoomId(roomId)
                    .orElseThrow(() -> new RuntimeException("Video room not found"));
                
                // Store session information
                userSessions.put(sessionId, userId);
                sessionRooms.put(sessionId, roomId);
                
                // Join Socket.IO room
                client.joinRoom(roomId);
                
                // Update video room service
                videoRoomService.joinVideoRoom(userId, roomId);
                
                // Notify other participants in the room
                client.getNamespace().getRoomOperations(roomId).sendEvent("user-joined", Map.of(
                    "userId", userId,
                    "sessionId", sessionId
                ));
                
                // Send current room participants to the new user
                VideoRoom updatedRoom = videoRoomRepository.findByRoomId(roomId).orElse(null);
                if (updatedRoom != null) {
                    client.sendEvent("room-participants", Map.of(
                        "participants", updatedRoom.getActiveParticipants()
                    ));
                }
                
                log.info("User {} joined room {} with session {}", userId, roomId, sessionId);
                
            } catch (Exception e) {
                log.error("Error joining room: {}", e.getMessage());
                client.sendEvent("error", Map.of("message", e.getMessage()));
            }
        };
    }
    
    private DataListener<Object> onLeaveRoom() {
        return (client, data, ackSender) -> {
            String sessionId = client.getSessionId().toString();
            String userId = userSessions.get(sessionId);
            String roomId = sessionRooms.get(sessionId);
            
            if (userId != null && roomId != null) {
                handleUserLeaveRoom(userId, roomId, sessionId);
            }
        };
    }
    
    private DataListener<Object> onWebRTCOffer() {
        return (client, data, ackSender) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            String roomId = sessionRooms.get(client.getSessionId().toString());
            String fromUserId = userSessions.get(client.getSessionId().toString());
            String toUserId = (String) dataMap.get("toUserId");
            
            if (roomId != null) {
                // Store signaling message
                storeSignalingMessage(roomId, fromUserId, toUserId, "offer", dataMap.get("offer"));
                
                // Forward offer to specific user or broadcast
                if (toUserId != null) {
                    forwardToUser(roomId, toUserId, "webrtc-offer", dataMap);
                } else {
                    client.getNamespace().getRoomOperations(roomId).sendEvent("webrtc-offer", dataMap);
                }
                
                log.info("WebRTC offer from {} to {} in room {}", fromUserId, toUserId, roomId);
            }
        };
    }
    
    private DataListener<Object> onWebRTCAnswer() {
        return (client, data, ackSender) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            String roomId = sessionRooms.get(client.getSessionId().toString());
            String fromUserId = userSessions.get(client.getSessionId().toString());
            String toUserId = (String) dataMap.get("toUserId");
            
            if (roomId != null) {
                // Store signaling message
                storeSignalingMessage(roomId, fromUserId, toUserId, "answer", dataMap.get("answer"));
                
                // Forward answer to specific user
                if (toUserId != null) {
                    forwardToUser(roomId, toUserId, "webrtc-answer", dataMap);
                }
                
                log.info("WebRTC answer from {} to {} in room {}", fromUserId, toUserId, roomId);
            }
        };
    }
    
    private DataListener<Object> onIceCandidate() {
        return (client, data, ackSender) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            String roomId = sessionRooms.get(client.getSessionId().toString());
            String fromUserId = userSessions.get(client.getSessionId().toString());
            String toUserId = (String) dataMap.get("toUserId");
            
            if (roomId != null) {
                // Store signaling message
                storeSignalingMessage(roomId, fromUserId, toUserId, "ice-candidate", dataMap.get("candidate"));
                
                // Forward ICE candidate to specific user or broadcast
                if (toUserId != null) {
                    forwardToUser(roomId, toUserId, "ice-candidate", dataMap);
                } else {
                    client.getNamespace().getRoomOperations(roomId).sendEvent("ice-candidate", dataMap);
                }
                
                log.debug("ICE candidate from {} to {} in room {}", fromUserId, toUserId, roomId);
            }
        };
    }
    
    private void handleUserLeaveRoom(String userId, String roomId, String sessionId) {
        try {
            // Leave Socket.IO room
            socketIOServer.getClient(java.util.UUID.fromString(sessionId)).leaveRoom(roomId);
            
            // Update video room service
            videoRoomService.leaveVideoRoom(userId, roomId);
            
            // Notify other participants
            socketIOServer.getRoomOperations(roomId).sendEvent("user-left", Map.of(
                "userId", userId,
                "sessionId", sessionId
            ));
            
            log.info("User {} left room {} with session {}", userId, roomId, sessionId);
            
        } catch (Exception e) {
            log.error("Error handling user leave: {}", e.getMessage());
        }
    }
    
    private void forwardToUser(String roomId, String toUserId, String eventName, Map<String, Object> data) {
        // Find session ID for the target user
        String targetSessionId = userSessions.entrySet().stream()
            .filter(entry -> toUserId.equals(entry.getValue()) && roomId.equals(sessionRooms.get(entry.getKey())))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
        
        if (targetSessionId != null) {
            try {
                socketIOServer.getClient(java.util.UUID.fromString(targetSessionId)).sendEvent(eventName, data);
            } catch (Exception e) {
                log.error("Error forwarding message to user {}: {}", toUserId, e.getMessage());
            }
        }
    }
    
    private void storeSignalingMessage(String roomId, String fromUserId, String toUserId, String type, Object data) {
        try {
            VideoRoom videoRoom = videoRoomRepository.findByRoomId(roomId).orElse(null);
            if (videoRoom != null) {
                VideoRoom.SignalingMessage message = new VideoRoom.SignalingMessage(
                    fromUserId, toUserId, type, data, LocalDateTime.now()
                );
                videoRoom.getSignalingHistory().add(message);
                videoRoomRepository.save(videoRoom);
            }
        } catch (Exception e) {
            log.error("Error storing signaling message: {}", e.getMessage());
        }
    }
}
