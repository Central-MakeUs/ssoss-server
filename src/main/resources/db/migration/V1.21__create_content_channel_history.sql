CREATE TABLE content_channel_history
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '채널별 콘텐츠 편집 히스토리 식별자',
    content_channel_id BIGINT       NOT NULL COMMENT '채널별 콘텐츠 id (content_channel.id)',
    title              VARCHAR(200) NULL COMMENT '이전 제목 (제목 있는 채널만)',
    body               TEXT         NOT NULL COMMENT '이전 본문',
    hashtags           JSON         NULL COMMENT '이전 해시태그 목록 (JSON 배열)',
    created_at         DATETIME(6)  NOT NULL COMMENT '적재 시각'
) COMMENT '채널별 콘텐츠 편집 히스토리';
