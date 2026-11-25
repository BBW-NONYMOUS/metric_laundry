-- Run once on an existing desktop schema. Preserves all payments and receipts.
-- Back up first. Run migrate_legacy.sql first only if still using the legacy schema.
USE metric_laundry;
ALTER TABLE payments ADD INDEX idx_pay_order (order_id);
ALTER TABLE payments DROP INDEX order_id;
ALTER TABLE payments MODIFY status ENUM('PENDING','PAID','VOID') NOT NULL DEFAULT 'PENDING';
