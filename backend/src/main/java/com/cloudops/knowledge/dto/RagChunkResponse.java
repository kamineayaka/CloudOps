package com.cloudops.knowledge.dto;

import com.cloudops.knowledge.domain.KnowledgeSourceType;

public record RagChunkResponse(
        Long id,
        KnowledgeSourceType sourceType,
        Long sourceId,
        int chunkIndex,
        String content,
        double similarity) {}
