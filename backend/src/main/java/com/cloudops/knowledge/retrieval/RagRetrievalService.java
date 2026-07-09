package com.cloudops.knowledge.retrieval;

import com.cloudops.common.config.RagProperties;
import com.cloudops.knowledge.indexing.EmbeddingException;
import com.cloudops.knowledge.indexing.EmbeddingProvider;
import com.cloudops.knowledge.indexing.EmbeddingProviderResolver;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RagRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(RagRetrievalService.class);

    private final RagProperties ragProperties;
    private final EmbeddingProviderResolver embeddingProviderResolver;
    private final KbChunkVectorRepository vectorRepository;

    public RagRetrievalService(
            RagProperties ragProperties,
            EmbeddingProviderResolver embeddingProviderResolver,
            KbChunkVectorRepository vectorRepository) {
        this.ragProperties = ragProperties;
        this.embeddingProviderResolver = embeddingProviderResolver;
        this.vectorRepository = vectorRepository;
    }

    public List<ScoredChunk> retrieve(String query) {
        return retrieve(query, null);
    }

    public List<ScoredChunk> retrieve(String query, Integer topKOverride) {
        if (!ragProperties.enabled() || query == null || query.isBlank()) {
            return List.of();
        }
        int topK = topKOverride != null && topKOverride > 0 ? topKOverride : ragProperties.topK();
        try {
            EmbeddingProvider provider = embeddingProviderResolver.active();
            float[] queryVector = provider.embed(query.strip());
            return vectorRepository.searchSimilar(
                    queryVector,
                    topK,
                    ragProperties.minSimilarity());
        } catch (EmbeddingException ex) {
            log.warn("RAG retrieval skipped: {}", ex.getMessage());
            return List.of();
        } catch (Exception ex) {
            log.warn("RAG retrieval failed: {}", ex.getMessage());
            return List.of();
        }
    }
}
