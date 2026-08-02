package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.WithdrawalCommand;
import com.ssoss.ssossbackend.auth.domain.service.SocialUnlinker;
import com.ssoss.ssossbackend.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final MemberService memberService;
    private final SocialUnlinker socialUnlinker;

    public void withdraw(WithdrawalCommand command) {
        memberService.withdraw(command.memberId(), command.reason(), command.detail());
        socialUnlinker.unlink(command.memberId());
    }
}
