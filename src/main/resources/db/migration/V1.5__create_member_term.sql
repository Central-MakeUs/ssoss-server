CREATE TABLE member_term (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '회원 약관 동의 식별자',
    member_id             BIGINT      NOT NULL COMMENT '동의한 회원 id (member.id)',
    service_terms_agreed  BOOLEAN     NOT NULL COMMENT '서비스 이용약관 동의 여부',
    privacy_policy_agreed BOOLEAN     NOT NULL COMMENT '개인정보 수집·이용 동의 여부',
    marketing_agreed      BOOLEAN     NOT NULL COMMENT '마케팅 수신 동의 여부',
    created_at            DATETIME(6) NOT NULL COMMENT '생성 시각',
    updated_at            DATETIME(6) NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_member_term_member_id UNIQUE (member_id)
) COMMENT '회원별 약관 동의 기록';
