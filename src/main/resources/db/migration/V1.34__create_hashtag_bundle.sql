CREATE TABLE hashtag_bundle
(
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '해시태그 묶음 식별자',
    name       VARCHAR(100) NOT NULL COMMENT '묶음 이름',
    hashtags   JSON         NOT NULL COMMENT '해시태그 목록 (JSON 배열)',
    created_at DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at DATETIME(6)  NOT NULL COMMENT '수정 시각'
) COMMENT '해시태그 묶음';

INSERT INTO hashtag_bundle (name, hashtags, created_at, updated_at)
VALUES ('카공 카페',
        '["#카공카페", "#노트북카페", "#콘센트많은카페", "#조용한카페", "#공부하기좋은카페", "#스터디카페"]',
        NOW(6), NOW(6)),
       ('이벤트/할인 홍보',
        '["#오픈이벤트", "#할인이벤트", "#신메뉴출시", "#1+1이벤트", "#선착순이벤트", "#오늘의쿠폰"]',
        NOW(6), NOW(6)),
       ('동네 고객 유입 해시태그',
        '["#00동카페", "#00역카페", "#00동맛집", "#00동디저트", "#동네카페", "#우리동네카페"]',
        NOW(6), NOW(6));
