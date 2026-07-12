DROP TABLE refresh_token;

CREATE TABLE refresh_token (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'refresh token 식별자',
    member_id  BIGINT      NOT NULL COMMENT '소유 회원 id (member.id)',
    session_id CHAR(36)    NOT NULL COMMENT '로그인 세션(회전 사슬) 식별자 (UUID)',
    token_hash CHAR(64)    NOT NULL COMMENT 'refresh token 의 SHA-256 해시(hex)',
    status     VARCHAR(20) NOT NULL COMMENT '토큰 상태 (ACTIVE/ROTATED)',
    expires_at DATETIME(6) NOT NULL COMMENT '만료 시각',
    deleted_at DATETIME(6) NULL COMMENT '소프트 삭제 시각 (NULL 이면 유효)',
    version    BIGINT      NOT NULL COMMENT '낙관적 잠금 버전',
    created_at DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_refresh_token_token_hash UNIQUE (token_hash),
    INDEX idx_refresh_token_session_id (session_id),
    INDEX idx_refresh_token_member_id (member_id)
) COMMENT '로그인 세션별 refresh token (RTR 회전 이력 포함)';
