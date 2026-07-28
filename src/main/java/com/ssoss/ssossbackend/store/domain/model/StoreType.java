package com.ssoss.ssossbackend.store.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum StoreType {

    CAFE,
    DESSERT_CAFE,
    BAKERY,
    BAKERY_CAFE,
    BRUNCH_CAFE,
    ROASTERY_CAFE,
    CAFE_BAR;

    public static StoreType from(String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }
}
