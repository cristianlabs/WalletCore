CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    name VARCHAR(255) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_accounts_user_id ON accounts (user_id);
