-- ============================================================
-- V9: Performance Indexes for Projects & Outbox
-- ============================================================

-- Partial indexes for active projects (deleted_at IS NULL)
CREATE INDEX IF NOT EXISTS idx_projects_active_status
    ON projects(status) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_projects_active_dates
    ON projects(start_date, end_date) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_projects_active_created_at
    ON projects(created_at DESC) WHERE deleted_at IS NULL;

-- Case-insensitive search acceleration
CREATE INDEX IF NOT EXISTS idx_projects_lower_name
    ON projects(LOWER(name));

CREATE INDEX IF NOT EXISTS idx_projects_lower_client_name
    ON projects(LOWER(client_name));

-- Assignment performance
CREATE INDEX IF NOT EXISTS idx_project_assignments_project_id
    ON project_assignments(project_id);

CREATE INDEX IF NOT EXISTS idx_project_assignments_instructor_id
    ON project_assignments(instructor_id);
