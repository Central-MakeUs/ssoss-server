CREATE TABLE member_withdrawal_history (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '탈퇴 이력 식별자',
    provider     VARCHAR(20) NOT NULL COMMENT '소셜 프로바이더 (NAVER 등)',
    social_id    VARCHAR(64) NOT NULL COMMENT '프로바이더가 발급한 사용자 식별자',
    withdrawn_at DATETIME(6) NOT NULL COMMENT '탈퇴 시각',
    created_at   DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at   DATETIME(6) NOT NULL COMMENT '수정 시각'
) COMMENT '탈퇴 이력';
