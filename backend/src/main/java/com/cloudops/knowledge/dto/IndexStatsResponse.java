package com.cloudops.knowledge.dto;

public record IndexStatsResponse(
        boolean ragEnabled,
        String embeddingProvider,
        Integer embeddingDims,
        long totalChunks,
        long architectureChunks,
        long workLogChunks,
        long manualChunks,
        String reindexHint) {}
