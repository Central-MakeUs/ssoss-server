UPDATE content
SET name = CONCAT(LEFT(name, 19), '…')
WHERE CHAR_LENGTH(name) = 21
  AND RIGHT(name, 1) = '…';
