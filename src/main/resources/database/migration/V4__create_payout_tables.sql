-- ============================================================
-- V4: Razorpay Payout Tables - ORBIT AI Protection
-- ============================================================

-- Add Razorpay fields to existing payroll_bank_details table
ALTER TABLE payroll_bank_details
    ADD COLUMN IF NOT EXISTS upi_id          VARCHAR(100),
    ADD COLUMN IF NOT EXISTS razorpay_contact_id     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS razorpay_fund_account_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS retry_count     INTEGER DEFAULT 0;

-- Payout Record Table (salary payout schedule)
CREATE TABLE IF NOT EXISTS payout_record (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID         NOT NULL,
    salary_amount      NUMERIC(15, 2) NOT NULL,
    salary_date        DATE         NOT NULL,
    status             VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    retry_count        INTEGER      DEFAULT 0,
    created_at         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payout_record_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Payout Transaction Table (actual Razorpay payout per record)
CREATE TABLE IF NOT EXISTS payout_transaction (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID         NOT NULL,
    payout_record_id   UUID         NOT NULL,
    razorpay_payout_id VARCHAR(100),
    amount             NUMERIC(15, 2) NOT NULL,
    mode               VARCHAR(50)  DEFAULT 'IMPS',
    status             VARCHAR(50)  DEFAULT 'PENDING',
    reference_id       VARCHAR(255),
    fund_account_id    VARCHAR(100),
    narration          VARCHAR(255),
    error_message      TEXT,
    failure_reason     TEXT,
    retry_count        INTEGER      DEFAULT 0,
    created_at         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payout_tx_user    FOREIGN KEY (user_id)          REFERENCES users(id)         ON DELETE CASCADE,
    CONSTRAINT fk_payout_tx_record  FOREIGN KEY (payout_record_id) REFERENCES payout_record(id) ON DELETE CASCADE
);

-- Audit Log Table
CREATE TABLE IF NOT EXISTS payout_audit_log (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type         VARCHAR(100) NOT NULL,
    entity_id          UUID,
    entity_type        VARCHAR(50),
    user_id            UUID,
    description        TEXT,
    metadata           TEXT,
    created_at         TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_payout_record_user_id   ON payout_record(user_id);
CREATE INDEX IF NOT EXISTS idx_payout_record_status     ON payout_record(status);
CREATE INDEX IF NOT EXISTS idx_payout_record_date       ON payout_record(salary_date);
CREATE INDEX IF NOT EXISTS idx_payout_tx_user_id        ON payout_transaction(user_id);
CREATE INDEX IF NOT EXISTS idx_payout_tx_status         ON payout_transaction(status);
CREATE INDEX IF NOT EXISTS idx_payout_tx_rzp_id         ON payout_transaction(razorpay_payout_id);
