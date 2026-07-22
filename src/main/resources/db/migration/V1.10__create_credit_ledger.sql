CREATE TABLE credit_ledger (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '크레딧 원장 행 식별자',
    member_id            BIGINT      NOT NULL COMMENT '회원 id (member.id)',
    amount               INT         NOT NULL COMMENT '부호 있는 크레딧 변동량 (차감은 음수)',
    generation_result_id BIGINT      NOT NULL COMMENT '차감을 일으킨 생성 결과 id',
    created_at           DATETIME(6) NOT NULL COMMENT '생성 시각',
    CONSTRAINT uk_credit_ledger_generation_result_id UNIQUE (generation_result_id),
    INDEX idx_credit_ledger_member_id_created_at (member_id, created_at)
) COMMENT '크레딧 원장 (차감 전용)';
