package com.fplassistant.repository;

import com.fplassistant.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    List<ChatMessageEntity> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<ChatMessageEntity> findTop10ByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
