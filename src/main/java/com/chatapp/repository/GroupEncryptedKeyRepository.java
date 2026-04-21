package com.chatapp.repository;

import com.chatapp.model.GroupEncryptedKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupEncryptedKeyRepository extends JpaRepository<GroupEncryptedKey, UUID> {

    Optional<GroupEncryptedKey> findByGroupIdAndUserId(UUID groupId, UUID userId);

    List<GroupEncryptedKey> findByGroupId(UUID groupId);

    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
}
