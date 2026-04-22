package com.chatapp.repository;

import com.chatapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m WHERE " +
            "((m.senderId = :user1 AND m.receiverId = :user2) OR " +
            "(m.senderId = :user2 AND m.receiverId = :user1)) " +
            "AND m.groupId IS NULL " +
            "ORDER BY m.timestamp ASC")
    List<Message> findDirectMessages(@Param("user1") UUID user1, @Param("user2") UUID user2);

    @Query("SELECT m FROM Message m WHERE m.groupId = :groupId ORDER BY m.timestamp ASC")
    List<Message> findByGroupId(@Param("groupId") UUID groupId);

    @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.status = 'SENT' AND m.groupId IS NULL")
    List<Message> findUndeliveredMessages(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT m.receiverId FROM Message m WHERE m.senderId = :userId AND m.groupId IS NULL " +
            "UNION " +
            "SELECT DISTINCT m.senderId FROM Message m WHERE m.receiverId = :userId AND m.groupId IS NULL")
    List<UUID> findConversationPartners(@Param("userId") UUID userId);
}
