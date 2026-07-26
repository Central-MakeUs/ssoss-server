ALTER TABLE content
    ADD COLUMN deleted_at DATETIME(6) NULL COMMENT '삭제 시각 (NULL 이면 활성)';
