package com.ssoss.ssossbackend.auth.infrastructure.gateway;

import java.util.Optional;

import com.ssoss.ssossbackend.auth.domain.contract.AccountClient;
import com.ssoss.ssossbackend.auth.domain.model.Account;
import com.ssoss.ssossbackend.member.entrypoint.gateway.MemberIdentityReply;
import com.ssoss.ssossbackend.member.entrypoint.gateway.MemberInternalGateway;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class InternalAccountClient implements AccountClient {

    private final MemberInternalGateway memberInternalGateway;

    @Override
    public Optional<Account> find(String provider, String socialId) {
        return memberInternalGateway.find(provider, socialId)
            .map(identity -> Account.of(identity.id(), identity.status(), identity.lastWithdrawnAt()));
    }

    @Override
    public Optional<Account> findById(Long memberId) {
        return memberInternalGateway.findById(memberId)
            .map(identity -> Account.of(identity.id(), identity.status(), identity.lastWithdrawnAt()));
    }

    @Override
    public Account register(String provider, String socialId, String email) {
        MemberIdentityReply identity = memberInternalGateway.register(provider, socialId, email);
        return Account.of(identity.id(), identity.status(), identity.lastWithdrawnAt());
    }

    @Override
    public Account signup(Long memberId, boolean ageOver14Agreed, boolean serviceTermsAgreed,
        boolean privacyPolicyAgreed) {
        MemberIdentityReply identity = memberInternalGateway.signup(memberId, ageOver14Agreed, serviceTermsAgreed,
            privacyPolicyAgreed);
        return Account.of(identity.id(), identity.status(), identity.lastWithdrawnAt());
    }

    @Override
    public void withdraw(Long memberId, String reason, String detail) {
        memberInternalGateway.withdraw(memberId, reason, detail);
    }

    @Override
    public Account recover(Long memberId) {
        MemberIdentityReply identity = memberInternalGateway.recover(memberId);
        return Account.of(identity.id(), identity.status(), identity.lastWithdrawnAt());
    }
}
