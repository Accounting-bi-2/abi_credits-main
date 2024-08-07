CREATE SEQUENCE credit_seq;

CREATE TABLE credit (
    id BIGINT NOT NULL DEFAULT nextval('credit_seq'),
    expiry_date DATE,
    amount NUMERIC(19, 2) NOT NULL, -- Assuming decimal precision and scale
    user_id BIGINT NOT NULL,
    transaction_id BIGINT,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    PRIMARY KEY (id)
);

-- Unique index for transactionId
CREATE UNIQUE INDEX idx_credit_transaction ON credit (transaction_id);


CREATE SEQUENCE transaction_seq;

CREATE TABLE transaction (
     id BIGINT NOT NULL DEFAULT nextval('transaction_seq'),
     amount NUMERIC(19, 2) NOT NULL, -- Precision and scale might need to be adjusted based on currency standards
     user_id BIGINT NOT NULL,
     conversionrate DOUBLE PRECISION NOT NULL, -- ISO 4217 currency codes are 3 letters
     type VARCHAR(50) NOT NULL, -- Assuming the TransactionType enum translates to a VARCHAR
     discount_amount NUMERIC(19, 2), -- Assuming same precision and scale as amount
     description TEXT,
     discount_description TEXT,
     expiry_date DATE NOT NULL,
     transaction_date DATE NOT NULL,
     purchase_id BIGINT,
     date_created TIMESTAMP,
     date_updated TIMESTAMP,
     PRIMARY KEY (id)
);

ALTER TABLE transaction ADD CONSTRAINT chk_transaction_type CHECK (type IN ('CREDIT', 'DEBIT'));

CREATE VIEW user_credits_view AS
SELECT
    user_id,
    COALESCE( SUM(amount) FILTER (WHERE type = 'CREDIT' AND date_part('month', expiry_date) = date_part('month', CURRENT_DATE) AND date_part('year', expiry_date) = date_part('year', CURRENT_DATE)), 0) AS credit_amount,
    COALESCE( SUM(amount) FILTER (WHERE type = 'DEBIT' AND date_part('month', transaction_date) = date_part('month', CURRENT_DATE) AND date_part('year', transaction_date) = date_part('year', CURRENT_DATE)), 0) AS debit_amount,
    COALESCE( SUM(amount) FILTER (WHERE type = 'CREDIT' AND date_part('month', expiry_date) = date_part('month', CURRENT_DATE) AND date_part('year', expiry_date) = date_part('year', CURRENT_DATE)), 0) -
    COALESCE( SUM(amount) FILTER (WHERE type = 'DEBIT' AND date_part('month', transaction_date) = date_part('month', CURRENT_DATE) AND date_part('year', transaction_date) = date_part('year', CURRENT_DATE)), 0) AS current_credits
FROM
    credits_schema.transaction
GROUP BY
    user_id;
