package com.chatapp.service;

import com.chatapp.dto.message.MessageResponse;
import com.chatapp.dto.message.SendMessageRequest;
import com.chatapp.dto.message.WebSocketMessagePayload;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.model.Message;
import com.chatapp.model.enums.MessageStatus;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Save an encrypted direct message (from REST API).
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        // Validate sender and receiver exist
        if (!userRepository.existsById(request.getSenderId())) {
            throw new ResourceNotFoundException("User", "id", request.getSenderId());
        }
        if (!userRepository.existsById(request.getReceiverId())) {
            throw new ResourceNotFoundException("User", "id", request.getReceiverId());
        }

        Message message = Message.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .encryptedPayload(request.getEncryptedPayload())
                .encryptedKey(request.getEncryptedKey())
                .status(MessageStatus.SENT)
                .build();

        message = messageRepository.save(message);
        log.info("Message saved: {} -> {}", request.getSenderId(), request.getReceiverId());

        return toMessageResponse(message);
    }

    /**
     * Save an encrypted message from WebSocket.
     */
    @Transactional
    public MessageResponse saveWebSocketMessage(WebSocketMessagePayload payload) {
        Message message = Message.builder()
                .senderId(payload.getSenderId())
                .receiverId(payload.getReceiverId())
                .groupId(payload.getGroupId())
                .encryptedPayload(payload.getEncryptedPayload())
                .encryptedKey(payload.getEncryptedKey())
                .status(MessageStatus.SENT)
                .build();

        message = messageRepository.save(message);
        return toMessageResponse(message);
    }

    /**
     * Save a group message.
     */
    @Transactional
    public MessageResponse saveGroupMessage(UUID groupId, UUID senderId, String encryptedPayload, String encryptedKey) {
        Message message = Message.builder()
                .senderId(senderId)
                .groupId(groupId)
                .encryptedPayload(encryptedPayload)
                .encryptedKey(encryptedKey)
                .status(MessageStatus.SENT)
                .build();

        message = messageRepository.save(message);
        log.info("Group message saved: sender={} group={}", senderId, groupId);
        return toMessageResponse(message);
    }

    /**
     * Get chat history between two users (direct messages only).
     */
    public List<MessageResponse> getChatHistory(UUID currentUserId, UUID otherUserId) {
        List<Message> messages = messageRepository.findDirectMessages(currentUserId, otherUserId);
        return messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all messages in a group.
     */
    public List<MessageResponse> getGroupMessages(UUID groupId) {
        List<Message> messages = messageRepository.findByGroupId(groupId);
        return messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update message status (DELIVERED, READ).
     */
    @Transactional
    public MessageResponse updateMessageStatus(UUID messageId, MessageStatus status) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));

        message.setStatus(status);
        message = messageRepository.save(message);
        log.info("Message {} status updated to {}", messageId, status);

        return toMessageResponse(message);
    }

    /**
     * Get undelivered messages for a user (for offline message delivery).
     */
    public List<MessageResponse> getUndeliveredMessages(UUID userId) {
        return messageRepository.findUndeliveredMessages(userId).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    private MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .groupId(message.getGroupId())
                .encryptedPayload(message.getEncryptedPayload())
                .encryptedKey(message.getEncryptedKey())
                .timestamp(message.getTimestamp())
                .status(message.getStatus())
                .build();
    }
}
