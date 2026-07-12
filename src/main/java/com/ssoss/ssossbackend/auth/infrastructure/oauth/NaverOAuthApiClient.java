package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginClient;
import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class NaverOAuthApiClient implements SocialLoginClient {

    private final NaverProfileHttpClient naverProfileHttpClient;

    @Override
    public SocialProvider provider() {
        return SocialProvider.NAVER;
    }

    @Override
    public SocialProfile fetchProfile(String accessToken) {
        return naverProfileHttpClient.fetchProfile("Bearer " + accessToken).toProfile();
    }
}
