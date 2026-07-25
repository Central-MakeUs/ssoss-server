package com.ssoss.ssossbackend.auth.domain.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginClient;
import com.ssoss.ssossbackend.auth.domain.contract.SocialLoginRepository;
import com.ssoss.ssossbackend.auth.domain.model.SocialProvider;

import org.springframework.stereotype.Service;

@Service
public class SocialUnlinker {

    private final SocialLoginRepository socialLoginRepository;
    private final Map<SocialProvider, SocialLoginClient> socialLoginClients;

    public SocialUnlinker(SocialLoginRepository socialLoginRepository, List<SocialLoginClient> socialLoginClients) {
        this.socialLoginRepository = socialLoginRepository;
        this.socialLoginClients = socialLoginClients.stream()
            .collect(Collectors.toUnmodifiableMap(SocialLoginClient::provider, Function.identity()));
    }

    public void unlink(Long memberId) {
        socialLoginRepository.findByMemberId(memberId)
            .ifPresent(socialLogin -> socialLoginClients.get(socialLogin.getProvider())
                .unlink(socialLogin.getRefreshToken()));
    }

    public void delete(Long memberId) {
        socialLoginRepository.findByMemberId(memberId).ifPresent(socialLogin -> {
            socialLoginClients.get(socialLogin.getProvider()).unlink(socialLogin.getRefreshToken());
            socialLoginRepository.delete(socialLogin);
        });
    }
}
