package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginClient;
import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AppleOAuthApiClient implements SocialLoginClient {

    private final AppleIdentityTokenVerifier appleIdentityTokenVerifier;

    @Override
    public SocialProvider provider() {
        return SocialProvider.APPLE;
    }

    @Override
    public SocialProfile fetchProfile(String accessToken) {
        return appleIdentityTokenVerifier.verify(accessToken);
    }
}
