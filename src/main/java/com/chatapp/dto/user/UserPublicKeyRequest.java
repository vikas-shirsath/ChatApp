package com.chatapp.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPublicKeyRequest {

    @NotBlank(message = "Public key is required")
    private String publicKey;
}
