package com.cloudops.ai.llm;

import com.cloudops.common.config.AiProperties;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LlmProviderResolver {

    private final AiProperties aiProperties;
    private final List<LlmProvider> providers;

    public LlmProviderResolver(AiProperties aiProperties, List<LlmProvider> providers) {
        this.aiProperties = aiProperties;
        this.providers = providers;
    }

    public LlmProvider active() {
        String wanted = aiProperties.defaultProvider();
        return providers.stream()
                .filter(p -> p.name().equals(wanted))
                .findFirst()
                .orElseGet(() -> providers.isEmpty()
                        ? null
                        : providers.getFirst());
    }
}
