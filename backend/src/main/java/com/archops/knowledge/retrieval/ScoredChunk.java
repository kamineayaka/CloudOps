package com.archops.knowledge.retrieval;

import com.archops.knowledge.domain.KnowledgeSourceType;
import java.util.List;

public record ScoredChunk(
        Long id,
        KnowledgeSourceType sourceType,
        Long sourceId,
        int chunkIndex,
        String content,
        String metadata,
        double similarity) {}
