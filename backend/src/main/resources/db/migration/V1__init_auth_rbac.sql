-- ArchOps AI Platform - initial schema (auth, RBAC)
-- Phase 1: users, roles, permissions and seed data

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS permissions (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    display_name    VARCHAR(128),
    rbac_tier       VARCHAR(16)  NOT NULL DEFAULT 'LOW',
    approval_policy VARCHAR(32)  NOT NULL DEFAULT 'MANUAL_A',
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Seed permissions
INSERT INTO permissions (code, description) VALUES
    ('user:read',   'View users'),
    ('user:write',  'Manage users'),
    ('asset:read',  'View assets'),
    ('asset:write', 'Manage assets'),
    ('terminal:open',  'Open web SSH terminal'),
    ('ai:chat',        'Use AI assistant'),
    ('audit:read',     'View audit log'),
    ('task:manage',    'Manage scheduled tasks')
ON CONFLICT (code) DO NOTHING;

-- Seed roles with permission sets
INSERT INTO roles (name, description) VALUES
    ('ADMIN',    'System administrator - full access'),
    ('OPERATOR', 'Operations operator - asset and terminal access'),
    ('VIEWER',   'Read-only viewer')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN'
  AND p.code IN ('user:read','user:write','asset:read','asset:write','terminal:open','ai:chat','audit:read','task:manage')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'OPERATOR'
  AND p.code IN ('asset:read','asset:write','terminal:open','ai:chat')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'VIEWER'
  AND p.code IN ('asset:read','audit:read')
ON CONFLICT DO NOTHING;

-- Seed default admin user (password: admin123 - BCrypt hash)
-- MUST be changed after first login in production.
INSERT INTO users (username, password, display_name, rbac_tier, approval_policy)
VALUES ('admin', '$2a$10$ZKsyX8rPAqNxG.PIldklOec0L/pWQhwUIl2XNsSztB0xT10BudseG', 'System Administrator', 'HIGH', 'MANUAL_A')
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
