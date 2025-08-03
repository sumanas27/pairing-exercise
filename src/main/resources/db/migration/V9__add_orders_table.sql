CREATE TABLE IF NOT EXISTS organisations_schema.orders (
    id VARCHAR(100) PRIMARY KEY,
    merchant_id VARCHAR(100) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT chk_orders_amount_positive CHECK (total_amount > 0),
    CONSTRAINT chk_orders_currency_format CHECK (LENGTH(currency) = 3)
);

CREATE INDEX idx_orders_merchant_id ON orders(merchant_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);