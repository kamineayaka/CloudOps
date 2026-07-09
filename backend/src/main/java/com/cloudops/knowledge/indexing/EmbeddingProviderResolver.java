package com.cloudops.knowledge.indexing;

import com.cloudops.common.config.RagProperties;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingProviderResolver {

    private final RagProperties ragProperties;
    private final Map<String, EmbeddingProvider> providersByName;

    public EmbeddingProviderResolver(RagProperties ragProperties, List<EmbeddingProvider> providers) {
        this.ragProperties = ragProperties;
        this.providersByName = providers.stream()
                .collect(Collectors.toMap(EmbeddingProvider::name, Function.identity()));
    }

    public EmbeddingProvider active() {
        EmbeddingProvider provider = providersByName.get(ragProperties.provider());
        if (provider == null) {
            throw new EmbeddingException("Unknown RAG embedding provider: " + ragProperties.provider());
        }
        return provider;
    }
}
