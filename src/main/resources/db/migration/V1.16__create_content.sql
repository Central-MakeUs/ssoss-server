CREATE TABLE content (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '콘텐츠 식별자',
    member_id            BIGINT       NOT NULL COMMENT '회원 id (member.id)',
    generation_id        BIGINT       NOT NULL COMMENT '원본 생성 작업 id (generation.id)',
    generation_result_id BIGINT       NOT NULL COMMENT '원본 생성 결과 id (generation_result.id)',
    channel              VARCHAR(20)  NOT NULL COMMENT '채널 (BLOG, INSTAGRAM, DAANGN_BIZ, THREADS)',
    title                VARCHAR(200) NULL COMMENT '제목 (제목 있는 채널만)',
    body                 TEXT         NOT NULL COMMENT '본문',
    hashtags             JSON         NULL COMMENT '해시태그 목록 (JSON 배열)',
    created_at           DATETIME(6)  NOT NULL COMMENT '저장 시각',
    deleted_at           DATETIME(6)  NULL COMMENT '삭제 시각 (NULL 이면 활성)',
    CONSTRAINT uk_content_generation_result_id UNIQUE (generation_result_id)
) COMMENT '콘텐츠';
