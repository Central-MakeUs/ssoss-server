package com.ssoss.ssossbackend.auth.application.command;

import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

public record SocialLoginCommand(SocialProvider provider, String accessToken, String refreshToken) {

    public static SocialLoginCommand of(String provider, String accessToken, String refreshToken) {
        return new SocialLoginCommand(SocialProvider.from(provider), accessToken, refreshToken);
    }
}
