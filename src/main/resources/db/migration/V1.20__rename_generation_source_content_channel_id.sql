ALTER TABLE generation
    CHANGE source_content_id source_content_channel_id BIGINT NULL COMMENT '채널 변환 원본 채널별 콘텐츠 id (content_channel.id, 신규 생성은 NULL)';
