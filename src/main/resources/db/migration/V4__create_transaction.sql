CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    account_id UUID NOT NULL REFERENCES accounts (id),
    category_id UUID NOT NULL REFERENCES categories (id),
    type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    description VARCHAR(255),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_transactions_user_id ON transactions (user_id);
CREATE INDEX idx_transactions_account_id ON transactions (account_id);
CREATE INDEX idx_transactions_category_id ON transactions (category_id);
