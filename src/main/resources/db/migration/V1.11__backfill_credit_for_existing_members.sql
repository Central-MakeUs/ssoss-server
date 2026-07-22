INSERT INTO credit (member_id, free_balance, charged_balance, created_at, updated_at)
SELECT id, 50, 0, NOW(6), NOW(6)
FROM member
WHERE status IN ('ACTIVE', 'WITHDRAWN');

INSERT INTO credit_ledger (member_id, type, amount, generation_result_id, created_at)
SELECT id, 'GRANT', 50, NULL, NOW(6)
FROM member
WHERE status IN ('ACTIVE', 'WITHDRAWN');
