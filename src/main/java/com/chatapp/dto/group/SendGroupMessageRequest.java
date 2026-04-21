package com.chatapp.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendGroupMessageRequest {

    @NotNull(message = "Sender ID is required")
    private UUID senderId;

    @NotBlank(message = "Encrypted payload is required")
    private String encryptedPayload;

    private String encryptedKey;
}
