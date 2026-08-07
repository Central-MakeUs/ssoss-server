ALTER TABLE content
    DROP INDEX uk_content_source;
ALTER TABLE content
    DROP COLUMN source_type;
ALTER TABLE content
    CHANGE source_id generation_id BIGINT NOT NULL COMMENT '생성 작업 id';
ALTER TABLE content
    ADD CONSTRAINT uk_content_generation_id UNIQUE (generation_id);

ALTER TABLE content_channel
    DROP INDEX uk_content_channel_source_generation_result_id;
ALTER TABLE content_channel
    CHANGE source_generation_result_id generation_result_id BIGINT NOT NULL COMMENT '원본 생성 결과 id';
ALTER TABLE content_channel
    ADD CONSTRAINT uk_content_channel_generation_result_id UNIQUE (generation_result_id);
