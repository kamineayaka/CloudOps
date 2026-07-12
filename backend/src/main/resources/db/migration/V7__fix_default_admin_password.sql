-- Fix default admin password hash: previous seed did not match documented password admin123.
UPDATE users
SET password = '$2a$10$ZKsyX8rPAqNxG.PIldklOec0L/pWQhwUIl2XNsSztB0xT10BudseG'
WHERE username = 'admin'
  AND password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
