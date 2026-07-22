-- ArchOps AI Platform - Phase 2: assets, SSH credentials, audit log

CREATE TABLE IF NOT EXISTS assets (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    kind        VARCHAR(32)  NOT NULL,
    host        VARCHAR(255),
    port        INTEGER,
    metadata    JSONB        NOT NULL DEFAULT '{}'::jsonb,
    parent_id   BIGINT       REFERENCES assets(id) ON DELETE SET NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_assets_kind ON assets(kind);
CREATE INDEX IF NOT EXISTS idx_assets_parent ON assets(parent_id);

CREATE TABLE IF NOT EXISTS ssh_credentials (
    id              BIGSERIAL PRIMARY KEY,
    asset_id        BIGINT       NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    username        VARCHAR(64)  NOT NULL,
    auth_type       VARCHAR(16)  NOT NULL,
    secret_cipher   BYTEA        NOT NULL,
    secret_iv       BYTEA        NOT NULL,
    passphrase_hash VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_ssh_cred_asset ON ssh_credentials(asset_id);

CREATE TABLE IF NOT EXISTS user_assets (
    user_id  BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    asset_id BIGINT  NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, asset_id)
);

CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGSERIAL PRIMARY KEY,
    actor_id    BIGINT,
    actor_name  VARCHAR(64),
    action      VARCHAR(64)  NOT NULL,
    resource    VARCHAR(128),
    risk_level  VARCHAR(16),
    status      VARCHAR(16)  NOT NULL,
    detail      JSONB        NOT NULL DEFAULT '{}'::jsonb,
    ip_address  VARCHAR(64),
    user_agent  VARCHAR(255),
    prev_hash   VARCHAR(64),
    curr_hash   VARCHAR(64)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_actor ON audit_log(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);

-- Prevent tampering: deny UPDATE and DELETE on audit_log rows
CREATE OR REPLACE FUNCTION prevent_audit_tamper() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_log is append-only: UPDATE and DELETE are not allowed';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS audit_no_update ON audit_log;
CREATE TRIGGER audit_no_update BEFORE UPDATE ON audit_log
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_tamper();

DROP TRIGGER IF EXISTS audit_no_delete ON audit_log;
CREATE TRIGGER audit_no_delete BEFORE DELETE ON audit_log
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_tamper();
