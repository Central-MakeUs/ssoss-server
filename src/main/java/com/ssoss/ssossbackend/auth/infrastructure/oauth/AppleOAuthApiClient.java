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
class AppleOAuthApiClient implements SocialLoginClient {

    private final AppleIdentityTokenVerifier appleIdentityTokenVerifier;
    private final AppleTokenHttpClient appleTokenHttpClient;
    private final AppleRevokeHttpClient appleRevokeHttpClient;
    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final String clientId;

    AppleOAuthApiClient(
        AppleIdentityTokenVerifier appleIdentityTokenVerifier,
        AppleTokenHttpClient appleTokenHttpClient,
        AppleRevokeHttpClient appleRevokeHttpClient,
        AppleClientSecretGenerator appleClientSecretGenerator,
        @Value("${auth.oauth.apple.client-id}") String clientId
    ) {
        this.appleIdentityTokenVerifier = appleIdentityTokenVerifier;
        this.appleTokenHttpClient = appleTokenHttpClient;
        this.appleRevokeHttpClient = appleRevokeHttpClient;
        this.appleClientSecretGenerator = appleClientSecretGenerator;
        this.clientId = clientId;
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.APPLE;
    }

    @Override
    public SocialProfile fetchProfile(String accessToken) {
        return appleIdentityTokenVerifier.verify(accessToken);
    }

    @Override
    public Optional<String> exchangeRefreshToken(String credential) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", appleClientSecretGenerator.generate());
        form.add("grant_type", "authorization_code");
        form.add("code", credential);
        try {
            return Optional.ofNullable(appleTokenHttpClient.exchange(form).refreshToken());
        } catch (RestClientException e) {
            log.warn("애플 authorization code 교환 실패", e);
            return Optional.empty();
        }
    }

    @Override
    public void unlink(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", appleClientSecretGenerator.generate());
        form.add("token", refreshToken);
        form.add("token_type_hint", "refresh_token");
        try {
            appleRevokeHttpClient.revoke(form);
        } catch (RestClientException e) {
            log.error("애플 연결 해제 실패", e);
        }
    }
}
