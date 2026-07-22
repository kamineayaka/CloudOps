package com.archops.knowledge.retrieval;

import com.archops.knowledge.domain.KnowledgeSourceType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KbChunkVectorRepository {

    private final JdbcTemplate jdbcTemplate;

    public KbChunkVectorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertChunk(
            KnowledgeSourceType sourceType,
            Long sourceId,
            int chunkIndex,
            String content,
            String metadataJson,
            float[] embedding) {
        String vector = com.archops.knowledge.indexing.VectorUtils.toPgVector(embedding);
        jdbcTemplate.update(
                """
                INSERT INTO kb_chunks (source_type, source_id, chunk_index, content, metadata, embedding)
                VALUES (?, ?, ?, ?, ?::jsonb, ?::vector)
                """,
                sourceType.name(),
                sourceId,
                chunkIndex,
                content,
                metadataJson,
                vector);
    }

    public void deleteAllChunks() {
        jdbcTemplate.update("DELETE FROM kb_chunks");
    }

    public List<ScoredChunk> searchSimilar(float[] queryEmbedding, int topK, double minSimilarity) {
        String vector = com.archops.knowledge.indexing.VectorUtils.toPgVector(queryEmbedding);
        return jdbcTemplate.query(
                """
                SELECT id, source_type, source_id, chunk_index, content, metadata::text,
                       1 - (embedding <=> ?::vector) AS similarity
                FROM kb_chunks
                WHERE embedding IS NOT NULL
                  AND 1 - (embedding <=> ?::vector) >= ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """,
                (rs, rowNum) -> mapRow(rs),
                vector,
                vector,
                minSimilarity,
                vector,
                topK);
    }

    private ScoredChunk mapRow(ResultSet rs) throws SQLException {
        return new ScoredChunk(
                rs.getLong("id"),
                KnowledgeSourceType.valueOf(rs.getString("source_type")),
                rs.getObject("source_id", Long.class),
                rs.getInt("chunk_index"),
                rs.getString("content"),
                rs.getString("metadata"),
                rs.getDouble("similarity"));
    }
}
