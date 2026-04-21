package com.chatapp.controller;

import com.chatapp.dto.group.*;
import com.chatapp.dto.message.MessageResponse;
import com.chatapp.model.GroupEncryptedKey;
import com.chatapp.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * Create a new group.
     */
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all groups a user belongs to.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupResponse>> getUserGroups(@PathVariable UUID userId) {
        List<GroupResponse> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * Add a member to a group.
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<Map<String, String>> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequest request
    ) {
        groupService.addMember(groupId, request);
        return ResponseEntity.ok(Map.of("message", "Member added successfully"));
    }

    /**
     * Remove a member from a group.
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId
    ) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }

    /**
     * Send an encrypted group message.
     */
    @PostMapping("/{groupId}/messages")
    public ResponseEntity<MessageResponse> sendGroupMessage(
            @PathVariable UUID groupId,
            @Valid @RequestBody SendGroupMessageRequest request
    ) {
        MessageResponse response = groupService.sendGroupMessage(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all messages in a group.
     */
    @GetMapping("/{groupId}/messages")
    public ResponseEntity<List<MessageResponse>> getGroupMessages(@PathVariable UUID groupId) {
        List<MessageResponse> messages = groupService.getGroupMessages(groupId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Store encrypted group keys for members.
     */
    @PostMapping("/{groupId}/keys")
    public ResponseEntity<Map<String, String>> storeGroupKeys(
            @PathVariable UUID groupId,
            @RequestBody List<GroupKeyPayload> keys
    ) {
        groupService.storeGroupKeys(groupId, keys);
        return ResponseEntity.ok(Map.of("message", "Group keys stored successfully"));
    }

    /**
     * Get all encrypted group keys for a group.
     */
    @GetMapping("/{groupId}/keys")
    public ResponseEntity<List<GroupEncryptedKey>> getGroupKeys(@PathVariable UUID groupId) {
        List<GroupEncryptedKey> keys = groupService.getGroupKeys(groupId);
        return ResponseEntity.ok(keys);
    }

    /**
     * Get a specific user's encrypted group key.
     */
    @GetMapping("/{groupId}/keys/{userId}")
    public ResponseEntity<GroupEncryptedKey> getUserGroupKey(
            @PathVariable UUID groupId,
            @PathVariable UUID userId
    ) {
        GroupEncryptedKey key = groupService.getUserGroupKey(groupId, userId);
        return ResponseEntity.ok(key);
    }
}
