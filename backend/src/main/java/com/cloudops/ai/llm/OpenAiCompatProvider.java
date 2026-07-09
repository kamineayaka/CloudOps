package com.cloudops.ai.llm;

import com.cloudops.common.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * LLM provider targeting any OpenAI-compatible Chat Completions endpoint
 * (OpenAI, DeepSeek, Together, vLLM, llama.cpp server, etc.).
 */
@Component
@ConditionalOnProperty(prefix = "cloudops.ai", name = "default-provider", havingValue = "openai-compat", matchIfMissing = true)
public class OpenAiCompatProvider implements LlmProvider {

    private final AiProperties.OpenAiCompatProviderConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatProvider(AiProperties properties, ObjectMapper objectMapper) {
        this.config = properties.openaiCompat();
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String name() {
        return "openai-compat";
    }

    @Override
    public CompletionResult complete(List<ChatMessage> messages, List<ToolDefinition> tools) {
        try {
            String body = buildRequestBody(messages, tools, false);
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder(URI.create(config.baseUrl() + "/chat/completions"))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + config.apiKey())
                            .timeout(Duration.ofMillis(config.timeoutMs()))
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                return new CompletionResult("[LLM error] " + response.statusCode() + ": " + response.body(), List.of());
            }
            return parseResponse(response.body());
        } catch (Exception ex) {
            return new CompletionResult("[LLM request failed] " + ex.getMessage(), List.of());
        }
    }

    @Override
    public void streamComplete(List<ChatMessage> messages, List<ToolDefinition> tools, Consumer<String> onToken) {
        try {
            String body = buildRequestBody(messages, tools, true);
            httpClient.send(
                    HttpRequest.newBuilder(URI.create(config.baseUrl() + "/chat/completions"))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + config.apiKey())
                            .timeout(Duration.ofMillis(config.timeoutMs()))
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build(),
                    HttpResponse.BodyHandlers.ofLines())
                    .body()
                    .forEach(line -> {
                        if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                            try {
                                JsonNode delta = objectMapper.readTree(line.substring(6))
                                        .path("choices").path(0).path("delta").path("content");
                                if (!delta.isMissingNode() && !delta.isNull()) {
                                    onToken.accept(delta.asText());
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    });
        } catch (Exception ex) {
            onToken.accept("[stream failed] " + ex.getMessage());
        }
    }

    private String buildRequestBody(List<ChatMessage> messages, List<ToolDefinition> tools, boolean stream) throws Exception {
        var root = objectMapper.createObjectNode();
        root.put("model", config.model());
        root.put("stream", stream);
        var msgs = root.putArray("messages");
        for (ChatMessage msg : messages) {
            var m = msgs.addObject();
            m.put("role", msg.role());
            m.put("content", msg.content());
        }
        if (tools != null && !tools.isEmpty()) {
            var toolsNode = root.putArray("tools");
            for (ToolDefinition tool : tools) {
                var t = toolsNode.addObject();
                t.put("type", "function");
                var fn = t.putObject("function");
                fn.put("name", tool.name());
                fn.put("description", tool.description());
                fn.put("parameters", objectMapper.readTree(tool.parametersJson()));
            }
        }
        return objectMapper.writeValueAsString(root);
    }

    private CompletionResult parseResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode choice = root.path("choices").path(0).path("message");
        String content = choice.path("content").asText("");
        List<ToolCall> toolCalls = new ArrayList<>();
        JsonNode tcNode = choice.path("tool_calls");
        if (tcNode.isArray()) {
            for (JsonNode tc : tcNode) {
                toolCalls.add(new ToolCall(
                        tc.path("id").asText(),
                        tc.path("function").path("name").asText(),
                        tc.path("function").path("arguments").asText()));
            }
        }
        return new CompletionResult(content, toolCalls);
    }
}
