UPDATE refresh_token
SET status = 'DELETED', deleted_at = updated_at
WHERE status = 'ROTATED';

ALTER TABLE refresh_token
    MODIFY status VARCHAR(20) NOT NULL COMMENT '토큰 상태 (ACTIVE/DELETED)';
