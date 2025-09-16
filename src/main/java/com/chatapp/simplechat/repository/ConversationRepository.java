package com.chatapp.simplechat.repository;

import com.chatapp.simplechat.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    
    @Query("SELECT c FROM Conversation c WHERE c.user1Username = :username OR c.user2Username = :username ORDER BY c.updatedAt DESC")
    List<Conversation> findByUserUsername(@Param("username") String username);
    
    @Query("SELECT c FROM Conversation c WHERE (c.user1Username = :user1 AND c.user2Username = :user2) OR (c.user1Username = :user2 AND c.user2Username = :user1)")
    Optional<Conversation> findByTwoUsers(@Param("user1") String user1, @Param("user2") String user2);
}