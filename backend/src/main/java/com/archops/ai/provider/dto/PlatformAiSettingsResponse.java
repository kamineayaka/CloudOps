package com.archops.ai.provider.dto;

public record PlatformAiSettingsResponse(
        Long defaultChatProviderId,
        Long defaultEmbeddingProviderId,
        boolean ragEnabled,
        int ragTopK,
        double ragMinSimilarity) {}
