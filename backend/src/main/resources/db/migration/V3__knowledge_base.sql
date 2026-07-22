-- ArchOps AI Platform - Phase 4: knowledge base (architecture snapshot, work log, RAG)

CREATE TABLE IF NOT EXISTS architecture_snapshot (
    id         BIGSERIAL PRIMARY KEY,
    version    BIGINT       NOT NULL,
    content    JSONB        NOT NULL,
    summary    TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_arch_version ON architecture_snapshot(version DESC);

CREATE TABLE IF NOT EXISTS work_log (
    id         BIGSERIAL PRIMARY KEY,
    log_type   VARCHAR(32)  NOT NULL,
    actor_id   BIGINT,
    actor_name VARCHAR(64),
    summary    TEXT         NOT NULL,
    diff       JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_work_log_type ON work_log(log_type);
CREATE INDEX IF NOT EXISTS idx_work_log_created ON work_log(created_at DESC);

CREATE TABLE IF NOT EXISTS kb_chunks (
    id              BIGSERIAL PRIMARY KEY,
    source_type     VARCHAR(32)  NOT NULL,
    source_id       BIGINT,
    chunk_index     INTEGER      NOT NULL,
    content         TEXT         NOT NULL,
    embedding       vector(1536),
    metadata        JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_kb_chunks_source ON kb_chunks(source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_kb_chunks_embedding ON kb_chunks
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
