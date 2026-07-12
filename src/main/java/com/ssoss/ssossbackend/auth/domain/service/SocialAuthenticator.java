package com.ssoss.ssossbackend.auth.domain.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginClient;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import org.springframework.stereotype.Service;

@Service
public class SocialAuthenticator {

    private final Map<SocialProvider, SocialLoginClient> clients;

    public SocialAuthenticator(List<SocialLoginClient> clients) {
        this.clients = clients.stream()
            .collect(Collectors.toUnmodifiableMap(SocialLoginClient::provider, Function.identity()));
    }

    public SocialProfile authenticate(SocialProvider provider, String accessToken) {
        SocialLoginClient client = clients.get(provider);
        if (client == null) {
            throw new BusinessException(AuthErrorCode.UNSUPPORTED_SOCIAL_PROVIDER);
        }
        return client.fetchProfile(accessToken);
    }
}
