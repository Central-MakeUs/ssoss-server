CREATE TABLE saved_template_history
(
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '저장한 템플릿 편집 히스토리 식별자',
    saved_template_id BIGINT       NOT NULL COMMENT '저장한 템플릿 식별자 (saved_template.id)',
    title             VARCHAR(100) NOT NULL COMMENT '이전 제목',
    body              TEXT         NOT NULL COMMENT '이전 본문',
    created_at        DATETIME(6)  NOT NULL COMMENT '적재 시각'
) COMMENT '저장한 템플릿 편집 히스토리';
