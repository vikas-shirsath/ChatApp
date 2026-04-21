package com.chatapp.controller;

import com.chatapp.dto.message.MessageResponse;
import com.chatapp.dto.message.WebSocketMessagePayload;
import com.chatapp.model.GroupMember;
import com.chatapp.service.GroupService;
import com.chatapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final GroupService groupService;

    /**
     * Handle direct (1-to-1) encrypted messages via WebSocket.
     * Client sends to: /app/chat.send
     * Server delivers to: /user/{receiverId}/queue/messages
     */
    @MessageMapping("/chat.send")
    public void sendDirectMessage(@Payload WebSocketMessagePayload payload) {
        log.info("WebSocket DM: {} -> {}", payload.getSenderId(), payload.getReceiverId());

        // Save encrypted message to database (no decryption)
        MessageResponse saved = messageService.saveWebSocketMessage(payload);

        // Forward encrypted message to receiver's personal queue
        messagingTemplate.convertAndSendToUser(
                payload.getReceiverId().toString(),
                "/queue/messages",
                saved
        );

        // Send delivery confirmation back to sender
        messagingTemplate.convertAndSendToUser(
                payload.getSenderId().toString(),
                "/queue/messages",
                saved
        );
    }

    /**
     * Handle group encrypted messages via WebSocket.
     * Client sends to: /app/chat.group
     * Server delivers to each group member: /user/{memberId}/queue/messages
     */
    @MessageMapping("/chat.group")
    public void sendGroupMessage(@Payload WebSocketMessagePayload payload) {
        log.info("WebSocket Group: sender={} group={}", payload.getSenderId(), payload.getGroupId());

        // Save encrypted group message
        MessageResponse saved = messageService.saveWebSocketMessage(payload);

        // Broadcast to all group members
        List<GroupMember> members = groupService.getGroupMembers(payload.getGroupId());
        for (GroupMember member : members) {
            messagingTemplate.convertAndSendToUser(
                    member.getUserId().toString(),
                    "/queue/messages",
                    saved
            );
        }
    }

    /**
     * Handle message status updates via WebSocket.
     * Client sends to: /app/chat.status
     */
    @MessageMapping("/chat.status")
    public void updateMessageStatus(@Payload MessageResponse statusUpdate) {
        log.info("Status update: message={} status={}", statusUpdate.getId(), statusUpdate.getStatus());

        if (statusUpdate.getId() != null && statusUpdate.getStatus() != null) {
            MessageResponse updated = messageService.updateMessageStatus(
                    statusUpdate.getId(), statusUpdate.getStatus());

            // Notify the sender about the status change
            messagingTemplate.convertAndSendToUser(
                    updated.getSenderId().toString(),
                    "/queue/status",
                    updated
            );
        }
    }
}
