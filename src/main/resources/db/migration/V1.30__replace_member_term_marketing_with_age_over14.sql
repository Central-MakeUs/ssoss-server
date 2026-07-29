ALTER TABLE member_term
    ADD COLUMN age_over14_agreed BOOLEAN NOT NULL DEFAULT TRUE COMMENT '만 14세 이상 동의 여부' AFTER member_id;

ALTER TABLE member_term
    ALTER COLUMN age_over14_agreed DROP DEFAULT;

ALTER TABLE member_term
    DROP COLUMN marketing_agreed;
