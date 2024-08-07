
CREATE SEQUENCE purchase_seq;

CREATE TABLE purchase (
      id BIGINT NOT NULL DEFAULT NEXTVAL('purchase_seq'),
      user_id BIGINT NOT NULL,
      status VARCHAR(50) NOT NULL,
      amount NUMERIC(19, 2) NOT NULL, -- Assuming amount with two decimal places
      currency VARCHAR(3) NOT NULL, -- Assuming standard ISO currency codes
      stripe_session_id VARCHAR(255), -- Assuming the Stripe session ID fits within this length
      date_created TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      date_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      session_url VARCHAR(2000) NOT NULL,
      CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED')) -- Optional: Check constraint for status
);

-- Assuming currency is always a valid ISO 4217 code
ALTER TABLE purchase ADD CONSTRAINT chk_purchase_currency CHECK (currency ~ '^[A-Z]{3}$');

-- Unique index on stripe_session_id, allowing NULL values
CREATE UNIQUE INDEX idx_stripe_session_id ON purchase (stripe_session_id) WHERE stripe_session_id IS NOT NULL;

-- Index on user_id and status
CREATE INDEX idx_user_id_status ON purchase (user_id, status);
