package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.RecoveryCommand;
import com.ssoss.ssossbackend.auth.application.result.RecoveryResult;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;
import com.ssoss.ssossbackend.auth.domain.service.TokenIssuer;
import com.ssoss.ssossbackend.member.application.service.MemberIdentity;
import com.ssoss.ssossbackend.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecoveryService {

    private final MemberService memberService;
    private final TokenIssuer tokenIssuer;

    public RecoveryResult recover(RecoveryCommand command) {
        MemberIdentity member = memberService.recover(command.memberId());
        MemberStatus status = MemberStatus.valueOf(member.status());
        LoginToken loginToken = tokenIssuer.issue(member.id(), status);
        return new RecoveryResult(status.name(), loginToken.accessToken(), loginToken.refreshToken());
    }
}
