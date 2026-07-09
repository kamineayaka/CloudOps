package com.cloudops.mcp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Registry of MCP tools available to the AI agent. Tools self-register as
 * Spring beans; the registry indexes them by name for dispatch.
 */
@Component
public class ToolRegistry {

    private final Map<String, McpTool> tools = new LinkedHashMap<>();

    public ToolRegistry(List<McpTool> registered) {
        for (McpTool tool : registered) {
            tools.put(tool.name(), tool);
        }
    }

    public List<McpTool> all() {
        return List.copyOf(tools.values());
    }

    public List<com.cloudops.ai.llm.LlmProvider.ToolDefinition> definitions() {
        return tools.values().stream()
                .map(t -> new com.cloudops.ai.llm.LlmProvider.ToolDefinition(t.name(), t.description(), t.parametersJson()))
                .toList();
    }

    public Optional<McpTool> find(String name) {
        return Optional.ofNullable(tools.get(name));
    }
}
