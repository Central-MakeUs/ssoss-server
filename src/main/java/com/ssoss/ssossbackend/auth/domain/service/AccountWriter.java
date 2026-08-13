package com.ssoss.ssossbackend.auth.domain.service;

import com.ssoss.ssossbackend.auth.domain.contract.AccountClient;
import com.ssoss.ssossbackend.auth.domain.model.Account;
import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountWriter {

    private final AccountClient accountClient;

    public Account registerIfAbsent(String provider, SocialProfile profile) {
        return accountClient.find(provider, profile.socialId())
            .orElseGet(() -> accountClient.register(provider, profile.socialId(), profile.emailForSignup()));
    }

    public Account signup(Long memberId, boolean ageOver14Agreed, boolean serviceTermsAgreed,
        boolean privacyPolicyAgreed) {
        return accountClient.signup(memberId, ageOver14Agreed, serviceTermsAgreed, privacyPolicyAgreed);
    }

    public void withdraw(Long memberId, String reason, String detail) {
        accountClient.withdraw(memberId, reason, detail);
    }

    public Account recover(Long memberId) {
        return accountClient.recover(memberId);
    }
}
