ALTER TABLE credit
    ADD COLUMN granted_cycle_at DATETIME(6) NULL COMMENT '마지막 무료 크레딧 지급이 속한 사이클 시작 시각 (사이클당 1회 지급 판정 근거, 전환 이전 행은 NULL)';
