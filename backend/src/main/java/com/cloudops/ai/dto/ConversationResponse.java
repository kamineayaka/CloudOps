package com.cloudops.ai.dto;

import java.time.Instant;

public record ConversationResponse(Long id, String title, Instant createdAt, Instant updatedAt) {}
