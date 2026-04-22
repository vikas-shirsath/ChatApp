package com.chatapp.controller;

import com.chatapp.dto.user.UserPublicKeyRequest;
import com.chatapp.dto.user.UserResponse;
import com.chatapp.model.User;
import com.chatapp.service.PresenceService;
import com.chatapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PresenceService presenceService;

    @GetMapping
    public ResponseEntity<java.util.List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(userService::toUserResponse)
                .toList());
    }

    @GetMapping("/search")
    public ResponseEntity<java.util.List<UserResponse>> searchUsers(@RequestParam String username) {
        return ResponseEntity.ok(userService.searchByUsername(username).stream()
                .map(userService::toUserResponse)
                .toList());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userService.toUserResponse(user));
    }

    @PutMapping("/{userId}/public-key")
    public ResponseEntity<Map<String, String>> updatePublicKey(
            @PathVariable UUID userId,
            @Valid @RequestBody UserPublicKeyRequest request
    ) {
        userService.updatePublicKey(userId, request.getPublicKey());
        return ResponseEntity.ok(Map.of("message", "Public key updated successfully"));
    }

    @GetMapping("/{userId}/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey(@PathVariable UUID userId) {
        String publicKey = userService.getPublicKey(userId);
        return ResponseEntity.ok(Map.of("publicKey", publicKey != null ? publicKey : ""));
    }

    @GetMapping("/{userId}/online")
    public ResponseEntity<Map<String, Boolean>> isOnline(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        boolean online = presenceService.isOnline(user.getUsername());
        return ResponseEntity.ok(Map.of("online", online));
    }
}
