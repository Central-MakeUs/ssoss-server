package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.SocialLoginCommand;
import com.ssoss.ssossbackend.auth.application.result.SocialLoginResult;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;
import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.auth.domain.service.SocialAuthenticator;
import com.ssoss.ssossbackend.auth.domain.service.TokenIssuer;
import com.ssoss.ssossbackend.member.application.service.MemberIdentity;
import com.ssoss.ssossbackend.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final SocialAuthenticator socialAuthenticator;
    private final MemberService memberService;
    private final TokenIssuer tokenIssuer;

    public SocialLoginResult login(SocialLoginCommand command) {
        SocialProfile profile = socialAuthenticator.authenticate(command.provider(), command.accessToken());
        String provider = command.provider().name();
        MemberIdentity member = memberService.find(provider, profile.socialId())
            .orElseGet(() -> memberService.register(provider, profile.socialId()));
        MemberStatus status = MemberStatus.valueOf(member.status());
        LoginToken loginToken = tokenIssuer.issue(member.id(), status);
        return new SocialLoginResult(status.name(), loginToken.accessToken(), loginToken.refreshToken());
    }
}
