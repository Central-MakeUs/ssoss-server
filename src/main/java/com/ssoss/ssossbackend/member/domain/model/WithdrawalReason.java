package com.ssoss.ssossbackend.member.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum WithdrawalReason {

    MISSING_FEATURE("원하는 기능이 없어요"),
    CONTENT_QUALITY("콘텐츠 품질이 기대와 달랐어요"),
    HARD_TO_USE("사용 방법이 어려웠어요"),
    RARELY_USED("자주 사용하지 않게 되었어요"),
    OTHER("기타");

    private final String description;

    WithdrawalReason(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public static WithdrawalReason from(String value) {
        return Arrays.stream(values())
            .filter(reason -> reason.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }
}
