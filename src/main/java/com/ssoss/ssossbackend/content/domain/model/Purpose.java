package com.ssoss.ssossbackend.content.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum Purpose {

    INFORMATION,
    EVENT_DISCOUNT,
    NEW_MENU_PROMOTION;

    public static Purpose from(String value) {
        return Arrays.stream(values())
            .filter(purpose -> purpose.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }
}
