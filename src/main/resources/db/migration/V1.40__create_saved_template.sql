CREATE TABLE saved_template
(
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '저장한 템플릿 식별자',
    member_id            BIGINT       NOT NULL COMMENT '회원 식별자',
    template_id          BIGINT       NOT NULL COMMENT '원본 템플릿 식별자 (추적용, 조회에는 쓰지 않음)',
    category             VARCHAR(20)  NOT NULL COMMENT '저장 시점에 복사한 분류 (NEW_MENU: 신메뉴, EVENT: 이벤트, STORE_INTRO: 매장 소개, NOTICE: 공지)',
    title                VARCHAR(100) NOT NULL COMMENT '저장 시점에 복사한 제목',
    description          VARCHAR(200) NOT NULL COMMENT '저장 시점에 복사한 설명',
    body                 TEXT         NOT NULL COMMENT '회원이 고쳐 저장한 본문',
    recommended_channels JSON         NOT NULL COMMENT '저장 시점에 복사한 추천 채널 목록 (JSON 배열: BLOG, INSTAGRAM, DAANGN_BIZ, THREADS)',
    created_at           DATETIME(6)  NOT NULL COMMENT '저장 시각',
    updated_at           DATETIME(6)  NOT NULL COMMENT '수정 시각',
    deleted_at           DATETIME(6)  NULL COMMENT '삭제 시각 (NULL 이면 활성)'
) COMMENT '회원이 저장한 템플릿';
