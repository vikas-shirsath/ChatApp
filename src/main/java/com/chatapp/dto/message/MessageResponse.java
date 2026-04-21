package com.chatapp.dto.message;

import com.chatapp.model.enums.MessageStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private UUID groupId;
    private String encryptedPayload;
    private String encryptedKey;
    private LocalDateTime timestamp;
    private MessageStatus status;
}
