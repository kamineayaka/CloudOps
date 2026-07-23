-- ML-1-07: ordered SSH jump / proxy chain on credentials (connection topology, not Architecture SSOT)

ALTER TABLE ssh_credentials
    ADD COLUMN IF NOT EXISTS jump_asset_ids JSONB NOT NULL DEFAULT '[]'::jsonb;
