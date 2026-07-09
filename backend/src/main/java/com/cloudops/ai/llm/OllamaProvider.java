package com.cloudops.ai.llm;

import com.cloudops.common.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * LLM provider for a self-hosted Ollama instance. Uses Ollama's OpenAI-compatible
 * /v1/chat/completions endpoint so the request shape mirrors the compat provider.
 */
@Component
@ConditionalOnProperty(prefix = "cloudops.ai", name = "default-provider", havingValue = "ollama")
public class OllamaProvider implements LlmProvider {

    private final AiProperties.OllamaProviderConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OllamaProvider(AiProperties properties, ObjectMapper objectMapper) {
        this.config = properties.ollama();
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public String name() {
        return "ollama";
    }

    @Override
    public CompletionResult complete(List<ChatMessage> messages, List<ToolDefinition> tools) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("model", config.model());
            root.put("stream", false);
            var msgs = root.putArray("messages");
            for (ChatMessage msg : messages) {
                var m = msgs.addObject();
                m.put("role", msg.role());
                m.put("content", msg.content());
            }
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder(URI.create(config.baseUrl() + "/v1/chat/completions"))
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofMillis(config.timeoutMs()))
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(root)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            JsonNode content = objectMapper.readTree(response.body()).path("choices").path(0).path("message").path("content");
            return new CompletionResult(content.asText(""), List.of());
        } catch (Exception ex) {
            return new CompletionResult("[Ollama request failed] " + ex.getMessage(), List.of());
        }
    }

    @Override
    public void streamComplete(List<ChatMessage> messages, List<ToolDefinition> tools, Consumer<String> onToken) {
        CompletionResult result = complete(messages, tools);
        onToken.accept(result.content());
    }
}
