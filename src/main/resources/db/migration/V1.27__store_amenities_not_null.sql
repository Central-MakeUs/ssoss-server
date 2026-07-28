UPDATE store
SET takeout_available     = COALESCE(takeout_available, 0),
    reservation_available = COALESCE(reservation_available, 0),
    parking_available     = COALESCE(parking_available, 0)
WHERE takeout_available IS NULL
   OR reservation_available IS NULL
   OR parking_available IS NULL;

ALTER TABLE store
    MODIFY takeout_available TINYINT(1) NOT NULL DEFAULT 0 COMMENT '포장 가능 여부',
    MODIFY reservation_available TINYINT(1) NOT NULL DEFAULT 0 COMMENT '예약 가능 여부',
    MODIFY parking_available TINYINT(1) NOT NULL DEFAULT 0 COMMENT '주차 가능 여부';
