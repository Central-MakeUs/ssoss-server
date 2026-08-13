package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.SignupCommand;
import com.ssoss.ssossbackend.auth.application.result.SignupResult;
import com.ssoss.ssossbackend.auth.domain.model.Account;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;
import com.ssoss.ssossbackend.auth.domain.service.AccountWriter;
import com.ssoss.ssossbackend.auth.domain.service.TokenIssuer;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final AccountWriter accountWriter;
    private final TokenIssuer tokenIssuer;

    public SignupResult signup(SignupCommand command) {
        Account account = accountWriter.signup(
            command.memberId(),
            command.ageOver14Agreed(),
            command.serviceTermsAgreed(),
            command.privacyPolicyAgreed());
        MemberStatus status = account.status();
        LoginToken loginToken = tokenIssuer.issue(account.id(), status);
        return new SignupResult(status.name(), loginToken.accessToken(), loginToken.refreshToken());
    }
}
