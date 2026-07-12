package com.ssoss.ssossbackend.auth.application.command;

import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

public record SocialLoginCommand(SocialProvider provider, String accessToken) {

    public static SocialLoginCommand of(String provider, String accessToken) {
        return new SocialLoginCommand(SocialProvider.from(provider), accessToken);
    }
}
