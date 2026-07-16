-- MySQL Schema Fix for strict mode compatibility
-- Run this script to add missing created_at/updated_at columns to existing tables

USE transport_db;

-- Fix employees table (add nullable columns first to avoid strict mode errors)
ALTER TABLE employees ADD COLUMN created_at TIMESTAMP NULL;
ALTER TABLE employees ADD COLUMN updated_at TIMESTAMP NULL;
UPDATE employees SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL OR created_at = '0000-00-00 00:00:00';
UPDATE employees SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL OR updated_at = '0000-00-00 00:00:00';

-- Fix admins table  
ALTER TABLE admins ADD COLUMN created_at TIMESTAMP NULL;
ALTER TABLE admins ADD COLUMN updated_at TIMESTAMP NULL;
UPDATE admins SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL OR created_at = '0000-00-00 00:00:00';
UPDATE admins SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL OR updated_at = '0000-00-00 00:00:00';

-- Fix routes table
ALTER TABLE routes ADD COLUMN created_at TIMESTAMP NULL;
ALTER TABLE routes ADD COLUMN updated_at TIMESTAMP NULL;
UPDATE routes SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL OR created_at = '0000-00-00 00:00:00';
UPDATE routes SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL OR updated_at = '0000-00-00 00:00:00';

-- Fix alerts table
ALTER TABLE alerts ADD COLUMN created_at TIMESTAMP NULL;
ALTER TABLE alerts ADD COLUMN updated_at TIMESTAMP NULL;
UPDATE alerts SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL OR created_at = '0000-00-00 00:00:00';
UPDATE alerts SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL OR updated_at = '0000-00-00 00:00:00';

-- Fix bookings table
ALTER TABLE bookings ADD COLUMN created_at TIMESTAMP NULL;
ALTER TABLE bookings ADD COLUMN updated_at TIMESTAMP NULL;
UPDATE bookings SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL OR created_at = '0000-00-00 00:00:00';
UPDATE bookings SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL OR updated_at = '0000-00-00 00:00:00';

-- Now set NOT NULL constraint for created_at (optional - required by AbstractAuditable)
ALTER TABLE employees MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE admins MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE routes MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE alerts MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE bookings MODIFY COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;