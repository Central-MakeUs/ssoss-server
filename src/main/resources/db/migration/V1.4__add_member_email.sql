ALTER TABLE member
    ADD COLUMN email VARCHAR(255) NOT NULL COMMENT '소셜 계정 이메일' AFTER social_id;
