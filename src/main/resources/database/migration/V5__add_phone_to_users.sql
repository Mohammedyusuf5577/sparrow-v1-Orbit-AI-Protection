-- ============================================================
-- V5: Add Phone Column to Users Table - ORBIT AI Protection
-- ============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
