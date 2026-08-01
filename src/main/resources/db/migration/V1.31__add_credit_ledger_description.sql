ALTER TABLE credit_ledger
    ADD COLUMN description VARCHAR(100) NULL COMMENT '변동 사유 문구' AFTER amount;

UPDATE credit_ledger
    JOIN generation ON generation.id = credit_ledger.generation_id
SET credit_ledger.description = CONCAT(
        CASE
            WHEN FIND_IN_SET('BLOG', generation.channels) > 0 THEN '블로그'
            WHEN FIND_IN_SET('INSTAGRAM', generation.channels) > 0 THEN '인스타그램'
            WHEN FIND_IN_SET('DAANGN_BIZ', generation.channels) > 0 THEN '당근 비즈'
            WHEN FIND_IN_SET('THREADS', generation.channels) > 0 THEN '스레드'
        END,
        IF(generation.channels LIKE '%,%',
           CONCAT(' 외 ',
                  LENGTH(generation.channels) - LENGTH(REPLACE(generation.channels, ',', '')),
                  '건'),
           ''),
        ' 콘텐츠 생성')
WHERE credit_ledger.type = 'DEDUCT';

UPDATE credit_ledger
SET description = CONCAT(
        MONTH(IF(DAY(CONVERT_TZ(created_at, '+00:00', '+09:00')) < 3,
                 CONVERT_TZ(created_at, '+00:00', '+09:00') - INTERVAL 1 MONTH,
                 CONVERT_TZ(created_at, '+00:00', '+09:00'))),
        '월 크레딧 지급')
WHERE type = 'GRANT';

UPDATE credit_ledger
SET description = CONCAT(
        MONTH(IF(DAY(CONVERT_TZ(created_at, '+00:00', '+09:00')) < 3,
                 CONVERT_TZ(created_at, '+00:00', '+09:00') - INTERVAL 2 MONTH,
                 CONVERT_TZ(created_at, '+00:00', '+09:00') - INTERVAL 1 MONTH)),
        '월 크레딧 소멸')
WHERE type = 'EXPIRE';

CREATE TEMPORARY TABLE tmp_signup_grant AS
SELECT MIN(id) AS id
FROM credit_ledger
WHERE type = 'GRANT'
GROUP BY member_id;

UPDATE credit_ledger
SET description = '가입 크레딧 지급'
WHERE id IN (SELECT id FROM tmp_signup_grant);

DROP TEMPORARY TABLE tmp_signup_grant;

UPDATE credit_ledger
SET description = CASE type
                      WHEN 'GRANT' THEN '크레딧 지급'
                      WHEN 'CHARGE' THEN '크레딧 충전'
                      WHEN 'DEDUCT' THEN '콘텐츠 생성'
                      ELSE '크레딧 소멸'
    END
WHERE description IS NULL;

ALTER TABLE credit_ledger
    MODIFY COLUMN description VARCHAR(100) NOT NULL COMMENT '변동 사유 문구';
