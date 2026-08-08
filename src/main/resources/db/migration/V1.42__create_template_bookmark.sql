CREATE TABLE template_bookmark
(
    id            BIGINT      AUTO_INCREMENT PRIMARY KEY COMMENT '추천 템플릿 북마크 식별자',
    member_id     BIGINT      NOT NULL COMMENT '회원 식별자',
    template_id   BIGINT      NOT NULL COMMENT '추천 템플릿 식별자',
    bookmarked_at DATETIME(6) NULL COMMENT '북마크를 한 시각',
    created_at    DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at    DATETIME(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_template_bookmark_member_id_template_id UNIQUE (member_id, template_id)
) COMMENT '회원이 북마크한 추천 템플릿';
