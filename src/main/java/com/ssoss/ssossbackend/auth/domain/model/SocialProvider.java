package com.ssoss.ssossbackend.auth.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;

public enum SocialProvider {

    NAVER;

    public static SocialProvider from(String value) {
        return Arrays.stream(values())
            .filter(provider -> provider.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER));
    }
}
