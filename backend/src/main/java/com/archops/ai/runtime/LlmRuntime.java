package com.archops.ai.runtime;

import com.archops.ai.llm.LlmProvider.ChatMessage;
import com.archops.ai.llm.LlmProvider.CompletionResult;
import com.archops.ai.llm.LlmProvider.ToolDefinition;
import java.util.List;
import java.util.function.Consumer;

public interface LlmRuntime {

    CompletionResult complete(List<ChatMessage> messages, List<ToolDefinition> tools);

    CompletionResult streamComplete(List<ChatMessage> messages, List<ToolDefinition> tools, Consumer<String> onToken);
}
