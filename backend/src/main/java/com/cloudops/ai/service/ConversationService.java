package com.cloudops.ai.service;

import com.cloudops.ai.domain.AiConversation;
import com.cloudops.ai.domain.AiMessage;
import com.cloudops.ai.dto.ChatMessageResponse;
import com.cloudops.ai.dto.ConversationResponse;
import com.cloudops.ai.repository.AiConversationRepository;
import com.cloudops.ai.repository.AiMessageRepository;
import com.cloudops.common.exception.BusinessException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationService {

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;

    public ConversationService(
            AiConversationRepository conversationRepository,
            AiMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ConversationResponse create(Long userId, String title) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(title != null ? title : "新对话");
        return toResponse(conversationRepository.save(conversation));
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> list(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AiConversation requireOwned(Long conversationId, Long userId) {
        AiConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "CONVERSATION_NOT_FOUND", "对话不存在"));
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "CONVERSATION_FORBIDDEN", "无权访问该对话");
        }
        return conversation;
    }

    @Transactional
    public AiMessage appendMessage(Long conversationId, String role, String content, String toolCallsJson) {
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setToolCalls(toolCallsJson != null ? toolCallsJson : "[]");
        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> history(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(m -> new ChatMessageResponse(m.getRole(), m.getContent(), m.getCreatedAt()))
                .toList();
    }

    private ConversationResponse toResponse(AiConversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt());
    }
}
