package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.SocialLoginCommand;
import com.ssoss.ssossbackend.auth.application.result.SocialLoginResult;
import com.ssoss.ssossbackend.auth.domain.model.Account;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;
import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.auth.domain.service.AccountWriter;
import com.ssoss.ssossbackend.auth.domain.service.SocialAuthenticator;
import com.ssoss.ssossbackend.auth.domain.service.SocialLoginWriter;
import com.ssoss.ssossbackend.auth.domain.service.SocialUnlinker;
import com.ssoss.ssossbackend.auth.domain.service.TokenIssuer;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final SocialAuthenticator socialAuthenticator;
    private final AccountWriter accountWriter;
    private final TokenIssuer tokenIssuer;
    private final SocialLoginWriter socialLoginWriter;
    private final SocialUnlinker socialUnlinker;

    public void delete(Long memberId) {
        socialUnlinker.delete(memberId);
    }

    public SocialLoginResult login(SocialLoginCommand command) {
        SocialProfile profile = socialAuthenticator.authenticate(command.provider(), command.accessToken());
        Account account = accountWriter.registerIfAbsent(command.provider().name(), profile);
        socialLoginWriter.save(account.id(), command.provider(), profile.socialId(), command.refreshToken());
        MemberStatus status = account.status();
        LoginToken loginToken = tokenIssuer.issue(account.id(), status);
        return new SocialLoginResult(status.name(), loginToken.accessToken(), loginToken.refreshToken());
    }
}
