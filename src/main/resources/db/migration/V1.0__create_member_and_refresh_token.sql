CREATE TABLE member (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '회원 식별자',
    provider   VARCHAR(20)  NOT NULL COMMENT '소셜 프로바이더 (NAVER 등)',
    social_id  VARCHAR(64)  NOT NULL COMMENT '프로바이더가 발급한 사용자 식별자',
    created_at DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at DATETIME(6)  NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_member_provider_social_id UNIQUE (provider, social_id)
) COMMENT '회원';

CREATE TABLE refresh_token (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'refresh token 식별자',
    member_id  BIGINT      NOT NULL COMMENT '소유 회원 id (member.id)',
    token_hash CHAR(64)    NOT NULL COMMENT 'refresh token 의 SHA-256 해시(hex)',
    created_at DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_refresh_token_member_id UNIQUE (member_id)
) COMMENT '회원별 refresh token';
