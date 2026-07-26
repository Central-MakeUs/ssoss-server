ALTER TABLE content_channel
    ADD COLUMN member_id BIGINT NULL COMMENT '회원 id (member.id)';

UPDATE content_channel
    JOIN content ON content.id = content_channel.content_id
SET content_channel.member_id = content.member_id;

ALTER TABLE content_channel
    MODIFY COLUMN member_id BIGINT NOT NULL COMMENT '회원 id (member.id)';
