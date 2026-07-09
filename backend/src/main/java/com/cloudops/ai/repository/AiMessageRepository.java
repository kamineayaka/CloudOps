package com.cloudops.ai.repository;

import com.cloudops.ai.domain.AiMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
