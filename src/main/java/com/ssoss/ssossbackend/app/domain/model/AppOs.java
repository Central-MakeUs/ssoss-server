package com.ssoss.ssossbackend.app.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;

public enum AppOs {

    IOS,
    ANDROID;

    public static AppOs from(String value) {
        return Arrays.stream(values())
            .filter(os -> os.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(AppErrorCode.UNSUPPORTED_APP_OS));
    }
}
