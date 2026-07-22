package com.archops.ai.llm;

import java.util.List;
import java.util.function.Consumer;

/**
 * Abstraction over LLM providers. Implementations include the OpenAI-compatible
 * API and a self-hosted Ollama backend. The platform picks the active provider
 * via {@code archops.ai.default-provider}.
 */
public interface LlmProvider {

    String name();

    /**
     * Synchronous completion with optional tool definitions. The returned
     * {@link CompletionResult} may carry either a textual answer or a list of
     * tool-call requests that the agent must execute and feed back.
     */
    CompletionResult complete(List<ChatMessage> messages, List<ToolDefinition> tools);

    /**
     * Streaming completion. Tokens are delivered to {@code onToken} as they
     * arrive. When the model requests tool calls, they arrive via
     * {@link CompletionResult#toolCalls()} after the stream completes.
     */
    void streamComplete(List<ChatMessage> messages, List<ToolDefinition> tools, Consumer<String> onToken);

    record ChatMessage(String role, String content, List<ToolCall> toolCalls) {
        public static ChatMessage system(String content) { return new ChatMessage("system", content, List.of()); }
        public static ChatMessage user(String content) { return new ChatMessage("user", content, List.of()); }
        public static ChatMessage assistant(String content) { return new ChatMessage("assistant", content, List.of()); }
        public static ChatMessage tool(String content) { return new ChatMessage("tool", content, List.of()); }
    }

    record ToolDefinition(String name, String description, String parametersJson) {}

    record ToolCall(String id, String name, String arguments) {}

    record CompletionResult(String content, List<ToolCall> toolCalls) {}
}
