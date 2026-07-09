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
public class OllamaEmbeddingProvider implements EmbeddingProvider {

    private final RagProperties.OllamaEmbeddingConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OllamaEmbeddingProvider(RagProperties properties, ObjectMapper objectMapper) {
        this.config = properties.ollama();
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String name() {
        return "ollama";
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
        List<float[]> vectors = new ArrayList<>(texts.size());
        for (String text : texts) {
            vectors.add(embedSingle(text));
        }
        return vectors;
    }

    private float[] embedSingle(String text) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("model", config.model());
            root.put("prompt", text);

            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder(URI.create(config.baseUrl() + "/api/embeddings"))
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofMillis(config.timeoutMs()))
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(root)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new EmbeddingException("Ollama embedding error " + response.statusCode() + ": " + response.body());
            }

            JsonNode embedding = objectMapper.readTree(response.body()).path("embedding");
            if (!embedding.isArray()) {
                throw new EmbeddingException("Ollama returned invalid embedding payload");
            }
            float[] values = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                values[i] = (float) embedding.get(i).asDouble();
            }
            if (values.length != dimensions()) {
                throw new EmbeddingException("Expected " + dimensions() + " dimensions, got " + values.length
                        + " — check cloudops.rag.ollama.dimensions matches model " + config.model());
            }
            return values;
        } catch (EmbeddingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new EmbeddingException("Ollama embedding request failed: " + ex.getMessage(), ex);
        }
    }
}
