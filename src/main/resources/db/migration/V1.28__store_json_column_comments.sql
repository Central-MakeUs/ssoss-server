ALTER TABLE store
    MODIFY business_days JSON NULL COMMENT '영업 요일 목록 (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)',
    MODIFY signature_menus JSON NULL COMMENT '대표 메뉴 목록',
    MODIFY keywords JSON NULL COMMENT '매장 키워드 목록';
