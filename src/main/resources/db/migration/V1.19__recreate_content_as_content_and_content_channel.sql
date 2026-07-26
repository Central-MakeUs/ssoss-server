DROP TABLE content;

CREATE TABLE content
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '콘텐츠 식별자',
    member_id   BIGINT      NOT NULL COMMENT '회원 id (member.id)',
    source_type VARCHAR(20) NOT NULL COMMENT '원본 종류 (GENERATION: 생성 작업, TEMPLATE: 추천 템플릿)',
    source_id   BIGINT      NOT NULL COMMENT '원본 id (source_type 이 GENERATION 이면 generation.id)',
    purpose     VARCHAR(30) NOT NULL COMMENT '목적 (INFORMATION, EVENT_DISCOUNT, NEW_MENU_PROMOTION)',
    tone        VARCHAR(20) NOT NULL COMMENT '톤 (CASUAL, EMOTIONAL, INFORMATIVE, PROMOTIONAL)',
    keywords    JSON        NULL COMMENT '키워드 목록 (JSON 배열)',
    created_at  DATETIME(6) NOT NULL COMMENT '저장 시각',
    CONSTRAINT uk_content_source UNIQUE (source_type, source_id)
) COMMENT '콘텐츠';

CREATE TABLE content_channel
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '채널별 콘텐츠 식별자',
    content_id                  BIGINT       NOT NULL COMMENT '콘텐츠 id (content.id)',
    channel                     VARCHAR(20)  NOT NULL COMMENT '채널 (BLOG, INSTAGRAM, DAANGN_BIZ, THREADS)',
    source_generation_result_id BIGINT       NULL COMMENT '원본 생성 결과 id (generation_result.id, 템플릿 유래는 NULL)',
    title                       VARCHAR(200) NULL COMMENT '제목 (제목 있는 채널만)',
    body                        TEXT         NOT NULL COMMENT '본문',
    hashtags                    JSON         NULL COMMENT '해시태그 목록 (JSON 배열)',
    created_at                  DATETIME(6)  NOT NULL COMMENT '저장 시각',
    updated_at                  DATETIME(6)  NOT NULL COMMENT '수정 시각',
    deleted_at                  DATETIME(6)  NULL COMMENT '삭제 시각 (NULL 이면 활성)',
    CONSTRAINT uk_content_channel_source_generation_result_id UNIQUE (source_generation_result_id),
    CONSTRAINT uk_content_channel_content_id_channel UNIQUE (content_id, channel)
) COMMENT '채널별 콘텐츠';
