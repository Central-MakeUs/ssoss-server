INSERT INTO store (member_id, created_at, updated_at)
SELECT id, NOW(6), NOW(6)
FROM member
WHERE status IN ('ACTIVE', 'WITHDRAWN');
