package com.cloudops.knowledge.indexing;

import com.cloudops.common.config.RagProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OpenAiEmbeddingProvider implements EmbeddingProvider {

    private final RagProperties.OpenAiEmbeddingConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiEmbeddingProvider(RagProperties properties, ObjectMapper objectMapper) {
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
    public int dimensions() {
        return config.dimensions();
    }

    @Override
    public float[] embed(String text) {
        return embedBatch(List.of(text)).getFirst();
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        if (texts.isEmpty()) {
            return List.of();
        }
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new EmbeddingException("OPENAI_API_KEY is not configured for RAG embeddings");
        }
        try {
            var root = objectMapper.createObjectNode();
            root.put("model", config.model());
            var input = root.putArray("input");
            texts.forEach(input::add);

            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder(URI.create(config.baseUrl() + "/embeddings"))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + config.apiKey())
                            .timeout(Duration.ofMillis(config.timeoutMs()))
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(root)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new EmbeddingException("Embedding API error " + response.statusCode() + ": " + response.body());
            }

            JsonNode data = objectMapper.readTree(response.body()).path("data");
            List<float[]> vectors = new ArrayList<>(texts.size());
            for (JsonNode item : data) {
                vectors.add(parseEmbedding(item.path("embedding")));
            }
            if (vectors.size() != texts.size()) {
                throw new EmbeddingException("Embedding API returned " + vectors.size() + " vectors for " + texts.size() + " inputs");
            }
            return vectors;
        } catch (EmbeddingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new EmbeddingException("OpenAI-compatible embedding request failed: " + ex.getMessage(), ex);
        }
    }

    private float[] parseEmbedding(JsonNode node) {
        if (!node.isArray()) {
            throw new EmbeddingException("Invalid embedding response: missing array");
        }
        float[] values = new float[node.size()];
        for (int i = 0; i < node.size(); i++) {
            values[i] = (float) node.get(i).asDouble();
        }
        if (values.length != dimensions()) {
            throw new EmbeddingException("Expected " + dimensions() + " dimensions, got " + values.length);
        }
        return values;
    }
}
