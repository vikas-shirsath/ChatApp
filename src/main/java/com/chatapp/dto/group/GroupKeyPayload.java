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
public class GroupKeyPayload {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Encrypted group key is required")
    private String encryptedGroupKey;
}
