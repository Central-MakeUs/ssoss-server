CREATE TABLE member_withdrawal_reason (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '탈퇴 사유 식별자',
    reason     VARCHAR(20) NULL COMMENT '탈퇴 사유 (MISSING_FEATURE, CONTENT_QUALITY, HARD_TO_USE, RARELY_USED, OTHER)',
    detail     VARCHAR(500) NULL COMMENT '기타 사유 자유 입력',
    created_at DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 시각'
) COMMENT '탈퇴 사유';
