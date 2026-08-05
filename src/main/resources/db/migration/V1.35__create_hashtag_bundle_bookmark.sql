CREATE TABLE hashtag_bundle_bookmark
(
    id            BIGINT      AUTO_INCREMENT PRIMARY KEY COMMENT '해시태그 묶음 북마크 식별자',
    member_id     BIGINT      NOT NULL COMMENT '회원 식별자',
    bundle_id     BIGINT      NOT NULL COMMENT '해시태그 묶음 식별자',
    bookmarked_at DATETIME(6) NULL COMMENT '북마크를 한 시각',
    created_at    DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at    DATETIME(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_hashtag_bundle_bookmark_member_id_bundle_id UNIQUE (member_id, bundle_id)
) COMMENT '회원이 북마크한 해시태그 묶음';
