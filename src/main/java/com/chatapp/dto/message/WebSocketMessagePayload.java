package com.chatapp.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessagePayload {

    @NotNull
    private UUID senderId;

    private UUID receiverId;

    private UUID groupId;

    @NotBlank
    private String encryptedPayload;

    private String encryptedKey;
}
