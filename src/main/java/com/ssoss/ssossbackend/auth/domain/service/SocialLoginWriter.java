package com.ssoss.ssossbackend.auth.domain.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginClient;
import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginRepository;
import com.ssoss.ssossbackend.auth.domain.model.SocialLogin;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

import org.springframework.stereotype.Service;

@Service
public class SocialLoginWriter {

    private final SocialLoginRepository socialLoginRepository;
    private final Map<SocialProvider, SocialLoginClient> socialLoginClients;

    public SocialLoginWriter(SocialLoginRepository socialLoginRepository, List<SocialLoginClient> socialLoginClients) {
        this.socialLoginRepository = socialLoginRepository;
        this.socialLoginClients = socialLoginClients.stream()
            .collect(Collectors.toUnmodifiableMap(SocialLoginClient::provider, Function.identity()));
    }

    public void save(Long memberId, SocialProvider provider, String socialId, String credential) {
        socialLoginClients.get(provider).exchangeRefreshToken(credential)
            .ifPresent(refreshToken -> socialLoginRepository.save(
                socialLoginRepository.findByMemberId(memberId)
                    .map(existing -> existing.refreshWith(refreshToken))
                    .orElseGet(() -> SocialLogin.of(memberId, provider, socialId, refreshToken))));
    }
}
