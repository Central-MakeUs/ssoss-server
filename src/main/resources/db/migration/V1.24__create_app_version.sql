CREATE TABLE app_version
(
    id              BIGINT      AUTO_INCREMENT PRIMARY KEY COMMENT '앱 최소 지원 버전 식별자',
    os              VARCHAR(20) NOT NULL COMMENT 'OS (IOS, ANDROID)',
    minimum_version VARCHAR(20) NOT NULL COMMENT '최소 지원 버전 (semver)',
    created_at      DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at      DATETIME(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_app_version_os UNIQUE (os)
) COMMENT '앱 최소 지원 버전';

INSERT INTO app_version (os, minimum_version, created_at, updated_at)
VALUES ('IOS', '1.0.0', NOW(6), NOW(6)),
       ('ANDROID', '1.0.0', NOW(6), NOW(6));
