package com.cloudops.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudops.rag")
public record RagProperties(
        boolean enabled,
        String provider,
        int topK,
        double minSimilarity,
        int chunkSize,
        int chunkOverlap,
        OpenAiEmbeddingConfig openaiCompat,
        OllamaEmbeddingConfig ollama) {

    public record OpenAiEmbeddingConfig(String baseUrl, String apiKey, String model, int dimensions, long timeoutMs) {}

    public record OllamaEmbeddingConfig(String baseUrl, String model, int dimensions, long timeoutMs) {}
}
