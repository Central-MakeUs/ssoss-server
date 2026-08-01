UPDATE credit
SET granted_cycle_at = CONVERT_TZ(
        DATE_FORMAT(IF(DAY(CONVERT_TZ(created_at, '+00:00', '+09:00')) < 3,
                       CONVERT_TZ(created_at, '+00:00', '+09:00') - INTERVAL 1 MONTH,
                       CONVERT_TZ(created_at, '+00:00', '+09:00')),
                    '%Y-%m-03 00:00:00'),
        '+09:00', '+00:00')
WHERE granted_cycle_at IS NULL;

ALTER TABLE credit
    MODIFY COLUMN granted_cycle_at DATETIME(6) NOT NULL COMMENT '마지막 무료 크레딧 지급이 속한 사이클 시작 시각 (사이클당 1회 지급 판정 근거)';
