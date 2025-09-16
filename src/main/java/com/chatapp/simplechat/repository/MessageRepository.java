package com.chatapp.simplechat.repository;

import com.chatapp.simplechat.model.Message;
import com.chatapp.simplechat.model.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") String conversationId);

    @Query("SELECT m FROM Message m WHERE (m.senderUsername = :user1 AND m.recipientUsername = :user2) OR (m.senderUsername = :user2 AND m.recipientUsername = :user1) ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.recipientUsername = :recipient AND m.status != 'READ'")
    Long countUnreadMessages(@Param("conversationId") String conversationId, @Param("recipient") String recipient);

    @Modifying
    @Query("UPDATE Message m SET m.status = :readStatus WHERE m.conversationId = :conversationId AND m.recipientUsername = :recipient AND m.status <> :readStatus")
    int markAsRead(@Param("conversationId") String conversationId,
                   @Param("recipient") String recipient,
                   @Param("readStatus") MessageStatus readStatus);
}
