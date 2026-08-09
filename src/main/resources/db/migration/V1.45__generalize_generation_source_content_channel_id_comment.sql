ALTER TABLE generation
    MODIFY source_content_channel_id BIGINT NULL COMMENT '원본 채널별 콘텐츠 id (content_channel.id, 원본 없는 생성은 NULL)';
