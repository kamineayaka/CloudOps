package com.archops.knowledge.dto;

import com.archops.knowledge.domain.KnowledgeSourceType;

public record RagChunkResponse(
        Long id,
        KnowledgeSourceType sourceType,
        Long sourceId,
        int chunkIndex,
        String content,
        double similarity) {}
