package com.archops.ai.dto;

import java.time.Instant;

public record ChatMessageResponse(String role, String content, Instant createdAt) {}
