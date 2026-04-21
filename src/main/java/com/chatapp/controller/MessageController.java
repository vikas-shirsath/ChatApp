package com.chatapp.controller;

import com.chatapp.dto.message.MessageResponse;
import com.chatapp.dto.message.MessageStatusUpdate;
import com.chatapp.dto.message.SendMessageRequest;
import com.chatapp.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Send an encrypted direct message via REST API.
     */
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get chat history between the authenticated user and another user.
     * The currentUserId should be extracted from JWT in production;
     * here we accept it as a query parameter for simplicity.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<MessageResponse>> getChatHistory(
            @PathVariable UUID userId,
            @RequestParam UUID currentUserId
    ) {
        List<MessageResponse> messages = messageService.getChatHistory(currentUserId, userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Update message status (DELIVERED, READ).
     */
    @PatchMapping("/{messageId}/status")
    public ResponseEntity<MessageResponse> updateStatus(
            @PathVariable UUID messageId,
            @Valid @RequestBody MessageStatusUpdate statusUpdate
    ) {
        MessageResponse response = messageService.updateMessageStatus(messageId, statusUpdate.getStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * Get undelivered messages for a user (for offline delivery).
     */
    @GetMapping("/undelivered/{userId}")
    public ResponseEntity<List<MessageResponse>> getUndelivered(@PathVariable UUID userId) {
        List<MessageResponse> messages = messageService.getUndeliveredMessages(userId);
        return ResponseEntity.ok(messages);
    }
}
