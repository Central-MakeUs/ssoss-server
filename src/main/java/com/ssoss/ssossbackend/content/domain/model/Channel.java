package com.ssoss.ssossbackend.content.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public enum Channel {

    BLOG(true),
    INSTAGRAM(false),
    DAANGN_BIZ(false),
    THREADS(false);

    private final boolean titled;

    Channel(boolean titled) {
        this.titled = titled;
    }

    public boolean hasTitle() {
        return titled;
    }

    public static Channel from(String value) {
        return Arrays.stream(values())
            .filter(channel -> channel.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }
}
