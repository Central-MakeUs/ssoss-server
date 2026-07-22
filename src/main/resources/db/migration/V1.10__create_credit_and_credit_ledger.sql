CREATE TABLE credit (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '크레딧 잔액 식별자',
    member_id       BIGINT      NOT NULL COMMENT '회원 id (member.id)',
    free_balance    INT         NOT NULL COMMENT '무료 크레딧 잔액 (사이클 종료 시 소멸)',
    charged_balance INT         NOT NULL COMMENT '충전 크레딧 잔액 (소멸 없음)',
    version         BIGINT      NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전',
    created_at      DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at      DATETIME(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_credit_member_id UNIQUE (member_id)
) COMMENT '회원별 크레딧 잔액 (원장 합의 구체화)';

CREATE TABLE credit_ledger (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '크레딧 원장 행 식별자',
    member_id            BIGINT      NOT NULL COMMENT '회원 id (member.id)',
    type                 VARCHAR(20) NOT NULL COMMENT '변동 유형 (GRANT: 지급, DEDUCT: 차감, EXPIRE: 소멸, CHARGE: 충전)',
    amount               INT         NOT NULL COMMENT '부호 있는 크레딧 변동량 (지급·충전은 양수, 차감·소멸은 음수)',
    generation_result_id BIGINT      NULL COMMENT '차감을 일으킨 생성 결과 id (차감 행만)',
    created_at           DATETIME(6) NOT NULL COMMENT '생성 시각',
    CONSTRAINT uk_credit_ledger_generation_result_id UNIQUE (generation_result_id),
    INDEX idx_credit_ledger_member_id_created_at (member_id, created_at)
) COMMENT '크레딧 원장 — 모든 크레딧 변동의 단일 장부';
