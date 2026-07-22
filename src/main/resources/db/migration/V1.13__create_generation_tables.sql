CREATE TABLE generation (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '생성 작업 식별자',
    member_id               BIGINT       NOT NULL COMMENT '회원 id (member.id)',
    channels                VARCHAR(100) NOT NULL COMMENT '선택 채널 목록 (쉼표 구분: BLOG, INSTAGRAM, DAANGN_BIZ, THREADS)',
    purpose                 VARCHAR(30)  NOT NULL COMMENT '목적 (INFORMATION: 정보성, EVENT_DISCOUNT: 이벤트/할인, NEW_MENU_PROMOTION: 신메뉴/홍보)',
    tone                    VARCHAR(20)  NOT NULL COMMENT '톤 (CASUAL: 일상형, EMOTIONAL: 감성형, INFORMATIVE: 정보형, PROMOTIONAL: 홍보형)',
    emphasis                VARCHAR(500) NOT NULL COMMENT '강조 내용',
    forbidden               VARCHAR(500) NULL COMMENT '금지 내용',
    keywords                VARCHAR(500) NULL COMMENT '키워드',
    photo_guide_checked     TINYINT(1)   NOT NULL COMMENT '사진 가이드 체크 여부',
    source_saved_content_id BIGINT       NULL COMMENT '채널 변환 원본 저장 콘텐츠 id (신규 생성은 NULL)',
    created_at              DATETIME(6)  NOT NULL COMMENT '생성 시각',
    finished_at             DATETIME(6)  NULL COMMENT '전 채널 완료 시각'
) COMMENT '생성 작업';

CREATE TABLE generation_result (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '생성 결과 식별자',
    generation_id        BIGINT       NOT NULL COMMENT '생성 작업 id (generation.id)',
    channel              VARCHAR(20)  NOT NULL COMMENT '채널 (BLOG, INSTAGRAM, DAANGN_BIZ, THREADS)',
    status               VARCHAR(20)  NOT NULL COMMENT '상태 (SUCCEEDED: 성공, RATE_LIMITED: 429, SERVER_ERROR: 5xx, CONNECTION_ERROR: 전송 오류, TIMEOUT: 시간 초과, EMPTY_OUTPUT: 빈 산출, DISCARDED_LATE: 지각 폐기)',
    title                VARCHAR(200) NULL COMMENT '제목 (성공한 블로그만)',
    body                 TEXT         NULL COMMENT '본문 (성공 행만)',
    hashtags             JSON         NULL COMMENT '해시태그 목록 (JSON 배열, 성공 행만)',
    response_time_millis BIGINT       NOT NULL COMMENT '응답 시간 (밀리초)',
    input_tokens         INT          NULL COMMENT '입력 토큰 사용량 (LLM 응답이 있을 때만)',
    output_tokens        INT          NULL COMMENT '출력 토큰 사용량 (LLM 응답이 있을 때만)',
    raw_response         TEXT         NULL COMMENT 'LLM 응답 원문 JSON',
    created_at           DATETIME(6)  NOT NULL COMMENT '생성 시각',
    CONSTRAINT uk_generation_result_generation_id_channel UNIQUE (generation_id, channel)
) COMMENT '생성 결과';

CREATE TABLE generation_lock (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '잠금 행 식별자',
    member_id  BIGINT      NOT NULL COMMENT '회원 id (member.id)',
    created_at DATETIME(6) NOT NULL COMMENT '생성 시각',
    CONSTRAINT uk_generation_lock_member_id UNIQUE (member_id)
) COMMENT '회원당 생성 잠금';
