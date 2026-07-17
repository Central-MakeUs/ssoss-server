package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.SignupCommand;
import com.ssoss.ssossbackend.auth.application.result.SignupResult;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;
import com.ssoss.ssossbackend.auth.domain.service.TokenIssuer;
import com.ssoss.ssossbackend.member.application.service.MemberIdentity;
import com.ssoss.ssossbackend.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberService memberService;
    private final TokenIssuer tokenIssuer;

    public SignupResult signup(SignupCommand command) {
        MemberIdentity member = memberService.signup(
            command.memberId(),
            command.serviceTermsAgreed(),
            command.privacyPolicyAgreed(),
            command.marketingAgreed());
        MemberStatus status = MemberStatus.valueOf(member.status());
        LoginToken loginToken = tokenIssuer.issue(member.id(), status);
        return new SignupResult(status.name(), loginToken.accessToken(), loginToken.refreshToken());
    }
}
