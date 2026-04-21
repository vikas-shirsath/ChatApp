package com.chatapp.dto.group;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {

    private UUID id;
    private String name;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private int memberCount;
}
