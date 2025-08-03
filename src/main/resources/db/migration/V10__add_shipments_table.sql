CREATE TABLE IF NOT EXISTS organisations_schema.shipments (
    id VARCHAR(100) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    shipped_at TIMESTAMP NOT NULL,
    tracking_number VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_shipped_at ON shipments(shipped_at);