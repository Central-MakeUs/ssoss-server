package com.ssoss.ssossbackend.content.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum Tone {

    CASUAL,
    EMOTIONAL,
    INFORMATIVE,
    PROMOTIONAL;

    public static Tone from(String value) {
        return Arrays.stream(values())
            .filter(tone -> tone.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }
}
