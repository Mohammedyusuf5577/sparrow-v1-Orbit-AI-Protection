-- ============================================================
-- V6: Add client_name and soft delete to Projects
-- ============================================================

ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS client_name VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS deleted_at  TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_projects_deleted_at ON projects(deleted_at);
CREATE INDEX IF NOT EXISTS idx_projects_client_name ON projects(client_name);
