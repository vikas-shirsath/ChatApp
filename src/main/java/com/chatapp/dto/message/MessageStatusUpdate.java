package com.chatapp.dto.message;

import com.chatapp.model.enums.MessageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageStatusUpdate {

    @NotNull(message = "Message ID is required")
    private UUID messageId;

    @NotNull(message = "Status is required")
    private MessageStatus status;
}
