ALTER TABLE generation
    CHANGE source_saved_content_id source_content_id BIGINT NULL COMMENT '채널 변환 원본 콘텐츠 id (신규 생성은 NULL)';
