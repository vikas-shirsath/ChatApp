package com.chatapp.service;

import com.chatapp.dto.group.*;
import com.chatapp.dto.message.MessageResponse;
import com.chatapp.exception.ResourceNotFoundException;
import com.chatapp.model.Group;
import com.chatapp.model.GroupEncryptedKey;
import com.chatapp.model.GroupMember;
import com.chatapp.model.enums.GroupRole;
import com.chatapp.repository.GroupEncryptedKeyRepository;
import com.chatapp.repository.GroupMemberRepository;
import com.chatapp.repository.GroupRepository;
import com.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupEncryptedKeyRepository groupEncryptedKeyRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    /**
     * Create a new group and add the creator as ADMIN.
     */
    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        if (!userRepository.existsById(request.getCreatedBy())) {
            throw new ResourceNotFoundException("User", "id", request.getCreatedBy());
        }

        Group group = Group.builder()
                .name(request.getName())
                .createdBy(request.getCreatedBy())
                .build();
        group = groupRepository.save(group);

        // Add creator as ADMIN
        GroupMember adminMember = GroupMember.builder()
                .groupId(group.getId())
                .userId(request.getCreatedBy())
                .role(GroupRole.ADMIN)
                .build();
        groupMemberRepository.save(adminMember);

        // Add initial members if provided
        if (request.getMemberIds() != null) {
            for (UUID memberId : request.getMemberIds()) {
                if (!memberId.equals(request.getCreatedBy()) && userRepository.existsById(memberId)) {
                    GroupMember member = GroupMember.builder()
                            .groupId(group.getId())
                            .userId(memberId)
                            .role(GroupRole.MEMBER)
                            .build();
                    groupMemberRepository.save(member);
                }
            }
        }

        log.info("Group created: {} by user {}", group.getName(), request.getCreatedBy());
        return toGroupResponse(group);
    }

    /**
     * Add a member to a group.
     */
    @Transactional
    public void addMember(UUID groupId, AddMemberRequest request) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group", "id", groupId);
        }
        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("User", "id", request.getUserId());
        }
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getUserId())) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .groupId(groupId)
                .userId(request.getUserId())
                .role(GroupRole.MEMBER)
                .build();
        groupMemberRepository.save(member);

        // Store encrypted group key if provided
        if (request.getEncryptedGroupKey() != null) {
            GroupEncryptedKey encKey = GroupEncryptedKey.builder()
                    .groupId(groupId)
                    .userId(request.getUserId())
                    .encryptedGroupKey(request.getEncryptedGroupKey())
                    .build();
            groupEncryptedKeyRepository.save(encKey);
        }

        log.info("User {} added to group {}", request.getUserId(), groupId);
    }

    /**
     * Remove a member from a group.
     */
    @Transactional
    public void removeMember(UUID groupId, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ResourceNotFoundException("GroupMember", "groupId+userId", groupId + "+" + userId);
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
        groupEncryptedKeyRepository.deleteByGroupIdAndUserId(groupId, userId);
        log.info("User {} removed from group {}", userId, groupId);
    }

    /**
     * Send an encrypted message to a group.
     */
    @Transactional
    public MessageResponse sendGroupMessage(UUID groupId, SendGroupMessageRequest request) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group", "id", groupId);
        }
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getSenderId())) {
            throw new IllegalArgumentException("User is not a member of this group");
        }

        return messageService.saveGroupMessage(
                groupId,
                request.getSenderId(),
                request.getEncryptedPayload(),
                request.getEncryptedKey()
        );
    }

    /**
     * Get all messages in a group.
     */
    public List<MessageResponse> getGroupMessages(UUID groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group", "id", groupId);
        }
        return messageService.getGroupMessages(groupId);
    }

    /**
     * Get all members of a group.
     */
    public List<GroupMember> getGroupMembers(UUID groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }

    /**
     * Store encrypted group keys for members.
     */
    @Transactional
    public void storeGroupKeys(UUID groupId, List<GroupKeyPayload> keys) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group", "id", groupId);
        }

        for (GroupKeyPayload payload : keys) {
            GroupEncryptedKey existing = groupEncryptedKeyRepository
                    .findByGroupIdAndUserId(groupId, payload.getUserId())
                    .orElse(null);

            if (existing != null) {
                existing.setEncryptedGroupKey(payload.getEncryptedGroupKey());
                groupEncryptedKeyRepository.save(existing);
            } else {
                GroupEncryptedKey encKey = GroupEncryptedKey.builder()
                        .groupId(groupId)
                        .userId(payload.getUserId())
                        .encryptedGroupKey(payload.getEncryptedGroupKey())
                        .build();
                groupEncryptedKeyRepository.save(encKey);
            }
        }

        log.info("Group keys stored for group {}", groupId);
    }

    /**
     * Get encrypted group keys for a group.
     */
    public List<GroupEncryptedKey> getGroupKeys(UUID groupId) {
        return groupEncryptedKeyRepository.findByGroupId(groupId);
    }

    /**
     * Get a user's encrypted group key.
     */
    public GroupEncryptedKey getUserGroupKey(UUID groupId, UUID userId) {
        return groupEncryptedKeyRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupEncryptedKey", "groupId+userId", groupId + "+" + userId));
    }

    /**
     * Get all groups a user belongs to.
     */
    public List<GroupResponse> getUserGroups(UUID userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        return memberships.stream()
                .map(gm -> groupRepository.findById(gm.getGroupId()).orElse(null))
                .filter(g -> g != null)
                .map(this::toGroupResponse)
                .collect(Collectors.toList());
    }

    private GroupResponse toGroupResponse(Group group) {
        long memberCount = groupMemberRepository.countByGroupId(group.getId());
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .createdBy(group.getCreatedBy())
                .createdAt(group.getCreatedAt())
                .memberCount((int) memberCount)
                .build();
    }
}
