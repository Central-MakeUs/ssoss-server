package com.ssoss.ssossbackend.store.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum StoreType {

    CAFE("카페"),
    DESSERT_CAFE("디저트 카페"),
    BAKERY("베이커리"),
    BAKERY_CAFE("베이커리 카페"),
    BRUNCH_CAFE("브런치 카페"),
    ROASTERY_CAFE("로스터리 카페"),
    CAFE_BAR("카페바");

    private final String koreanName;

    StoreType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String koreanName() {
        return koreanName;
    }

    public static StoreType from(String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }
}
