CREATE TABLE social_login (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'social login 식별자',
    member_id     BIGINT       NOT NULL COMMENT '소유 회원 id (member.id)',
    provider      VARCHAR(20)  NOT NULL COMMENT '소셜 프로바이더 (NAVER/APPLE)',
    social_id     VARCHAR(255) NOT NULL COMMENT '프로바이더가 부여한 소셜 계정 식별자',
    refresh_token VARCHAR(512) NOT NULL COMMENT '연결 해제에 쓰는 프로바이더 리프레시 토큰',
    created_at    DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at    DATETIME(6)  NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_social_login_member_id UNIQUE (member_id)
) COMMENT '회원의 소셜 로그인 연결 (회원당 1행)';
