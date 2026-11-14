-- ============================================================
-- V7: Add assigned_by to Project Assignments
-- ============================================================

ALTER TABLE project_assignments
    ADD COLUMN IF NOT EXISTS assigned_by UUID REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_project_assignments_assigned_by ON project_assignments(assigned_by);
