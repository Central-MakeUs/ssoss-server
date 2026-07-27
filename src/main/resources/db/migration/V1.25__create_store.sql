CREATE TABLE store
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '매장 식별자',
    member_id             BIGINT       NOT NULL COMMENT '회원 id (member.id)',
    name                  VARCHAR(50)  NULL COMMENT '매장명',
    type                  VARCHAR(30)  NULL COMMENT '매장 유형 (CAFE, DESSERT_CAFE, BAKERY, BAKERY_CAFE, BRUNCH_CAFE, ROASTERY_CAFE, CAFE_BAR)',
    address               VARCHAR(200) NULL COMMENT '도로명 주소',
    introduction          VARCHAR(100) NULL COMMENT '매장 한 줄 소개',
    business_days         JSON         NULL COMMENT '영업 요일 목록 (JSON 배열: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)',
    open_time             VARCHAR(5)   NULL COMMENT '영업 시작 시각 (HH:mm)',
    close_time            VARCHAR(5)   NULL COMMENT '영업 종료 시각 (HH:mm)',
    signature_menus       JSON         NULL COMMENT '대표 메뉴 목록 (JSON 배열)',
    takeout_available     TINYINT(1)   NULL COMMENT '포장 가능 여부',
    reservation_available TINYINT(1)   NULL COMMENT '예약 가능 여부',
    parking_available     TINYINT(1)   NULL COMMENT '주차 가능 여부',
    strength              VARCHAR(500) NULL COMMENT '매장 강점',
    keywords              JSON         NULL COMMENT '매장 키워드 목록 (JSON 배열, # 없이 저장)',
    forbidden             VARCHAR(500) NULL COMMENT '금지 내용',
    tone                  VARCHAR(20)  NULL COMMENT '콘텐츠 작성 톤 (CASUAL, EMOTIONAL, INFORMATIVE, PROMOTIONAL)',
    created_at            DATETIME(6)  NOT NULL COMMENT '생성 시각',
    updated_at            DATETIME(6)  NOT NULL COMMENT '수정 시각',
    CONSTRAINT uk_store_member_id UNIQUE (member_id)
) COMMENT '매장';
