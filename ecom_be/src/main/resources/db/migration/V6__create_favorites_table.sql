-- Add facebook_account_id if not exists
SET @dbname = DATABASE();
SET @tablename = 'users';

SET @col1 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'facebook_account_id');
SET @sql1 = IF(@col1 = 0, 'ALTER TABLE users ADD COLUMN facebook_account_id VARCHAR(255)', 'SELECT 1');
PREPARE stmt FROM @sql1; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add google_account_id if not exists
SET @col2 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'google_account_id');
SET @sql2 = IF(@col2 = 0, 'ALTER TABLE users ADD COLUMN google_account_id VARCHAR(255)', 'SELECT 1');
PREPARE stmt FROM @sql2; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add profile_image if not exists
SET @col3 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'profile_image');
SET @sql3 = IF(@col3 = 0, 'ALTER TABLE users ADD COLUMN profile_image VARCHAR(255)', 'SELECT 1');
PREPARE stmt FROM @sql3; EXECUTE stmt; DEALLOCATE PREPARE stmt;