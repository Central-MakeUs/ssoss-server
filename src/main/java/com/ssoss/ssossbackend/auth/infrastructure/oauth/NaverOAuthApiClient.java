package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import java.util.Optional;

import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginClient;
import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
class NaverOAuthApiClient implements SocialLoginClient {

    private final NaverProfileHttpClient naverProfileHttpClient;
    private final NaverRevokeHttpClient naverRevokeHttpClient;
    private final String clientId;
    private final String clientSecret;

    NaverOAuthApiClient(
        NaverProfileHttpClient naverProfileHttpClient,
        NaverRevokeHttpClient naverRevokeHttpClient,
        @Value("${auth.oauth.naver.client-id}") String clientId,
        @Value("${auth.oauth.naver.client-secret}") String clientSecret
    ) {
        this.naverProfileHttpClient = naverProfileHttpClient;
        this.naverRevokeHttpClient = naverRevokeHttpClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.NAVER;
    }

    @Override
    public SocialProfile fetchProfile(String accessToken) {
        return naverProfileHttpClient.fetchProfile("Bearer " + accessToken).toProfile();
    }

    @Override
    public Optional<String> exchangeRefreshToken(String credential) {
        return Optional.of(credential);
    }

    @Override
    public void unlink(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");
        try {
            naverRevokeHttpClient.revoke(form);
        } catch (RestClientException e) {
            log.error("네이버 연결 해제 실패", e);
        }
    }
}
