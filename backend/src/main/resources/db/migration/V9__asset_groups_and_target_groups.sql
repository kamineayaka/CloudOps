-- ML-1-01: asset groups + conversation target groups

CREATE TABLE IF NOT EXISTS asset_group (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_asset_group_name ON asset_group (name);

CREATE TABLE IF NOT EXISTS asset_group_member (
    group_id BIGINT NOT NULL REFERENCES asset_group(id) ON DELETE CASCADE,
    asset_id BIGINT NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    PRIMARY KEY (group_id, asset_id)
);

CREATE INDEX IF NOT EXISTS idx_asset_group_member_asset ON asset_group_member (asset_id);

ALTER TABLE ai_conversations
    ADD COLUMN IF NOT EXISTS target_group_ids JSONB NOT NULL DEFAULT '[]'::jsonb;
