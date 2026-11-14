-- ============================================================
-- V2__create_payments_table.sql
-- Razorpay payment records for salary disbursement tracking
-- ============================================================

CREATE TABLE IF NOT EXISTS payments
(
    id                   UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    employee_payroll_id  UUID           NOT NULL,
    razorpay_order_id    VARCHAR(100)   NOT NULL UNIQUE,
    razorpay_payment_id  VARCHAR(100),
    razorpay_signature   VARCHAR(255),
    amount_paise         BIGINT         NOT NULL,
    amount_inr           NUMERIC(12, 2) NOT NULL,
    currency             VARCHAR(10)    NOT NULL DEFAULT 'INR',
    status               VARCHAR(20)    NOT NULL DEFAULT 'CREATED',
    description          VARCHAR(500),
    failure_reason       VARCHAR(500),
    created_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_payment_employee_payroll
        FOREIGN KEY (employee_payroll_id) REFERENCES employee_payroll (id) ON DELETE CASCADE,

    CONSTRAINT ck_payment_status
        CHECK (status IN ('CREATED', 'SUCCESS', 'FAILED', 'REFUNDED'))
);

CREATE INDEX IF NOT EXISTS idx_payment_razorpay_order_id
    ON payments (razorpay_order_id);

CREATE INDEX IF NOT EXISTS idx_payment_employee_payroll_id
    ON payments (employee_payroll_id);
