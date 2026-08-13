package com.ssoss.ssossbackend.member.entrypoint.gateway;

import java.util.Optional;

import com.ssoss.ssossbackend.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberInternalGateway {

    private final MemberService memberService;

    public Optional<MemberIdentityReply> find(String provider, String socialId) {
        return memberService.find(provider, socialId).map(MemberIdentityReply::from);
    }

    public Optional<MemberIdentityReply> findById(Long memberId) {
        return memberService.findById(memberId).map(MemberIdentityReply::from);
    }

    public MemberIdentityReply register(String provider, String socialId, String email) {
        return MemberIdentityReply.from(memberService.register(provider, socialId, email));
    }

    public MemberIdentityReply signup(Long memberId, boolean ageOver14Agreed, boolean serviceTermsAgreed,
        boolean privacyPolicyAgreed) {
        return MemberIdentityReply.from(memberService.signup(memberId, ageOver14Agreed, serviceTermsAgreed,
            privacyPolicyAgreed));
    }

    public void withdraw(Long memberId, String reason, String detail) {
        memberService.withdraw(memberId, reason, detail);
    }

    public MemberIdentityReply recover(Long memberId) {
        return MemberIdentityReply.from(memberService.recover(memberId));
    }
}
