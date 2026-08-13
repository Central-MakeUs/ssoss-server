package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.RecoveryCommand;
import com.ssoss.ssossbackend.auth.application.result.RecoveryResult;
import com.ssoss.ssossbackend.auth.domain.model.Account;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;
import com.ssoss.ssossbackend.auth.domain.service.AccountWriter;
import com.ssoss.ssossbackend.auth.domain.service.TokenIssuer;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecoveryService {

    private final AccountWriter accountWriter;
    private final TokenIssuer tokenIssuer;

    public RecoveryResult recover(RecoveryCommand command) {
        Account account = accountWriter.recover(command.memberId());
        MemberStatus status = account.status();
        LoginToken loginToken = tokenIssuer.issue(account.id(), status);
        return new RecoveryResult(status.name(), loginToken.accessToken(), loginToken.refreshToken());
    }
}
