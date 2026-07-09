package com.cloudops.knowledge.dto;

public record IndexStatsResponse(
        boolean ragEnabled,
        String embeddingProvider,
        long totalChunks,
        long architectureChunks,
        long workLogChunks,
        long manualChunks) {}
