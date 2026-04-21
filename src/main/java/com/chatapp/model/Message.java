package com.chatapp.model;

import com.chatapp.model.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_sender", columnList = "senderId"),
        @Index(name = "idx_message_receiver", columnList = "receiverId"),
        @Index(name = "idx_message_group", columnList = "groupId"),
        @Index(name = "idx_message_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID senderId;

    private UUID receiverId;

    private UUID groupId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedPayload;

    @Column(columnDefinition = "TEXT")
    private String encryptedKey;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;
}
