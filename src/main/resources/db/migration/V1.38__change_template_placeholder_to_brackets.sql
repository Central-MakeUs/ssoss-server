UPDATE template
SET body = REPLACE(REPLACE(body, '{', '['), '}', ']')
WHERE body LIKE '%{%';
