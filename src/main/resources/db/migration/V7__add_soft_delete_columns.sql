-- Soft Delete: Add is_deleted column to core tables
-- When a record is "deleted", is_deleted is set to 1 instead of removing the row

ALTER TABLE products ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0;
