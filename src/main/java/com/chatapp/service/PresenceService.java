package com.chatapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PresenceService {

    // Track online users: userId -> set of session IDs (user may have multiple connections)
    private final ConcurrentHashMap<String, Set<String>> onlineUsers = new ConcurrentHashMap<>();
    // Reverse map: sessionId -> username
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        String sessionId = accessor.getSessionId();

        if (user != null && sessionId != null) {
            String username = user.getName();
            onlineUsers.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
            sessionUserMap.put(sessionId, username);
            log.info("User connected: {} (session: {})", username, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        if (sessionId != null) {
            String username = sessionUserMap.remove(sessionId);
            if (username != null) {
                Set<String> sessions = onlineUsers.get(username);
                if (sessions != null) {
                    sessions.remove(sessionId);
                    if (sessions.isEmpty()) {
                        onlineUsers.remove(username);
                        log.info("User went offline: {}", username);
                    }
                }
            }
        }
    }

    public boolean isOnline(UUID userId) {
        // This is a simplified check — in production, you'd map userId to username
        return onlineUsers.values().stream().anyMatch(sessions -> !sessions.isEmpty());
    }

    public boolean isOnline(String username) {
        Set<String> sessions = onlineUsers.get(username);
        return sessions != null && !sessions.isEmpty();
    }

    public Set<String> getOnlineUsernames() {
        return onlineUsers.keySet();
    }
}
