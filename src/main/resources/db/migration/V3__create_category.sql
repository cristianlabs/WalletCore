CREATE TABLE categories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, name)
);

CREATE INDEX idx_categories_user_id ON categories (user_id);
