package com.cloudops.knowledge.dto;

import java.time.Instant;

public record WorkLogResponse(Long id, String logType, String actorName, String summary, Instant createdAt) {}
