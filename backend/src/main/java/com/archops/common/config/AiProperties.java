package com.archops.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "archops.ai")
public record AiProperties(
        String defaultProvider,
        OpenAiCompatProviderConfig openaiCompat,
        OllamaProviderConfig ollama) {

    public record OpenAiCompatProviderConfig(String baseUrl, String apiKey, String model, long timeoutMs) {}

    public record OllamaProviderConfig(String baseUrl, String model, long timeoutMs) {}
}
