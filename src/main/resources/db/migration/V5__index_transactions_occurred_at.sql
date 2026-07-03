-- Supports date-range queries scoped to a user (dashboard/report endpoints), which
-- would otherwise fall back to a full scan as the transactions table grows.
CREATE INDEX idx_transactions_user_id_occurred_at ON transactions (user_id, occurred_at);
