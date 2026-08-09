ALTER TABLE content
    ADD COLUMN name VARCHAR(100) NULL COMMENT '콘텐츠 이름 (목록 카드에 보여줄 이름)' AFTER member_id;

UPDATE content c
    JOIN (SELECT cc.content_id,
                 TRIM(REGEXP_REPLACE(
                         CASE
                             WHEN cc.channel = 'BLOG' AND cc.title IS NOT NULL THEN cc.title
                             ELSE REGEXP_REPLACE(cc.body, '</?photo-guide(?=[[:space:]/>])[^>]*>', '')
                             END,
                         '[ \\t\\n\\r\\f\\x{0B}]+', ' ')) AS value,
                 ROW_NUMBER() OVER (
                     PARTITION BY cc.content_id
                     ORDER BY cc.deleted_at IS NOT NULL,
                         FIELD(cc.channel, 'BLOG', 'INSTAGRAM', 'DAANGN_BIZ', 'THREADS')) AS pick
          FROM content_channel cc) representative
    ON representative.content_id = c.id AND representative.pick = 1
SET c.name = CASE
                 WHEN CHAR_LENGTH(representative.value) > 20 THEN CONCAT(LEFT(representative.value, 20), '…')
                 ELSE representative.value END;

UPDATE content
SET name = ''
WHERE name IS NULL;

ALTER TABLE content
    MODIFY COLUMN name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '콘텐츠 이름 (목록 카드에 보여줄 이름)';
