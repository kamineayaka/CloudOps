package com.cloudops.ai.repository;

import com.cloudops.ai.domain.AiConversation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
