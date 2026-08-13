package com.ssoss.ssossbackend.auth.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.auth.domain.model.Account;

public interface AccountClient {

    Optional<Account> find(String provider, String socialId);

    Optional<Account> findById(Long memberId);

    Account register(String provider, String socialId, String email);

    Account signup(Long memberId, boolean ageOver14Agreed, boolean serviceTermsAgreed,
        boolean privacyPolicyAgreed);

    void withdraw(Long memberId, String reason, String detail);

    Account recover(Long memberId);
}
