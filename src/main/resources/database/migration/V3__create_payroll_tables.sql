-- Payroll Module Schema for PostgreSQL

-- Employee Bank Details Table
CREATE TABLE IF NOT EXISTS employee_bank_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    account_holder_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(255) NOT NULL, -- To be encrypted
    ifsc VARCHAR(20) NOT NULL,
    upi_id VARCHAR(100),
    razorpay_contact_id VARCHAR(100),
    razorpay_fund_account_id VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_bank FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Payroll Table
CREATE TABLE IF NOT EXISTS payroll (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    salary_amount NUMERIC(15, 2) NOT NULL,
    salary_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PROCESSED, FAILED
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_payroll FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Payout Transaction Table
CREATE TABLE IF NOT EXISTS payout_transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    payroll_id UUID NOT NULL,
    payout_id VARCHAR(100), -- Razorpay Payout ID
    amount NUMERIC(15, 2) NOT NULL,
    mode VARCHAR(50) DEFAULT 'IMPS', -- IMPS, NEFT, RTGS, UPI
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, SUCCESS, FAILED, PROCESSING
    reference_id VARCHAR(255),
    narration VARCHAR(255),
    error_message TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_payout FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_payout FOREIGN KEY (payroll_id) REFERENCES payroll(id) ON DELETE CASCADE
);
