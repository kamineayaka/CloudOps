-- ML-3-07: session-scoped execution grants (permission memory for EXECUTION tools only)

CREATE TABLE IF NOT EXISTS execution_grant (
    id                       BIGSERIAL PRIMARY KEY,
    user_id                  BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    conversation_id          BIGINT       NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    tool_name                VARCHAR(64)  NOT NULL,
    asset_id                 BIGINT       REFERENCES assets(id) ON DELETE CASCADE,
    risk_level               VARCHAR(16)  NOT NULL,
    pattern                  VARCHAR(512),
    expires_at               TIMESTAMPTZ  NOT NULL,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by_approval_id   BIGINT       REFERENCES approvals(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_execution_grant_lookup
    ON execution_grant (user_id, conversation_id, tool_name, expires_at);

CREATE INDEX IF NOT EXISTS idx_execution_grant_expires
    ON execution_grant (expires_at);
