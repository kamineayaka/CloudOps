-- Allow embedding vectors from different providers (OpenAI 1536-dim, Ollama 768-dim, etc.)

DROP INDEX IF EXISTS idx_kb_chunks_embedding;

ALTER TABLE kb_chunks
    ALTER COLUMN embedding TYPE vector USING embedding::vector;
