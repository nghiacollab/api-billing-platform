CREATE TABLE IF NOT EXISTS consumers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    billing_type VARCHAR(20),
    wallet_balance NUMERIC(15,4),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS merchants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    merchant_code VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS merchant_apis (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT REFERENCES merchants(id),
    api_name VARCHAR(100),
    api_path VARCHAR(255),
    target_endpoint VARCHAR(512),
    unit_price NUMERIC(15,4),
    unit_type VARCHAR(50),
    status VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS consumer_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    consumer_id BIGINT REFERENCES consumers(id),
    merchant_api_id BIGINT REFERENCES merchant_apis(id),
    api_key VARCHAR(255) UNIQUE,
    auth_type VARCHAR(255),
    status VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    consumer_id BIGINT REFERENCES consumers(id),
    billing_cycle VARCHAR(255),
    total_amount NUMERIC(15,4),
    status VARCHAR(255) DEFAULT 'UNPAID',
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT REFERENCES invoices(id),
    merchant_api_id BIGINT REFERENCES merchant_apis(id),
    total_quantity NUMERIC(15,4),
    total_amount NUMERIC(15,4),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transaction_logs (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    subscription_id BIGINT NOT NULL REFERENCES consumer_subscriptions(id),
    request_time TIMESTAMP,
    snapshot_unit_price NUMERIC(15,4) NOT NULL,
    quantity NUMERIC(15,4) DEFAULT 1.0000,
    amount NUMERIC(15,4) NOT NULL,
    status VARCHAR(20),
    error_code VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_transaction_logs_request_time ON transaction_logs(request_time);
