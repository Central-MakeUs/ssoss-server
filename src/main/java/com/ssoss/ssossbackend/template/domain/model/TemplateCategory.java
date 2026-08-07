package com.ssoss.ssossbackend.template.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum TemplateCategory {

    NEW_MENU,
    EVENT,
    STORE_INTRO,
    NOTICE;

    public static TemplateCategory from(String value) {
        return Arrays.stream(values())
            .filter(category -> category.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }
}
