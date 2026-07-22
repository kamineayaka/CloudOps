package com.archops.knowledge.indexing;

import java.util.List;

/**
 * Abstraction for text embedding models used by the RAG pipeline.
 * Implementations target OpenAI-compatible /v1/embeddings and Ollama /api/embeddings.
 */
public interface EmbeddingProvider {

    String name();

    int dimensions();

    float[] embed(String text);

    List<float[]> embedBatch(List<String> texts);
}
