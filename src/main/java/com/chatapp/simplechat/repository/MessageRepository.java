package com.chatapp.simplechat.repository;

import com.chatapp.simplechat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findTop20ByOrderByCreatedAtDesc();
}