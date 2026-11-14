-- ============================================================
-- V8: Create notification_outbox Table - ORBIT DB Outbox
-- ============================================================

CREATE TABLE IF NOT EXISTS notification_outbox (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id       UUID         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    channel          VARCHAR(50)  NOT NULL DEFAULT 'WHATSAPP',
    recipient_phone  VARCHAR(20)  NOT NULL,
    template_name    VARCHAR(100) NOT NULL,
    payload          TEXT         NOT NULL,
    status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    attempts         INTEGER      NOT NULL DEFAULT 0,
    max_attempts     INTEGER      NOT NULL DEFAULT 3,
    next_retry_at    TIMESTAMP,
    sent_at          TIMESTAMP,
    error_log        TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outbox_poll 
    ON notification_outbox(status, next_retry_at, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_user_project 
    ON notification_outbox(user_id, project_id);
