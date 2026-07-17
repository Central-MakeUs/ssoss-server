package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.WithdrawalCommand;
import com.ssoss.ssossbackend.member.application.service.MemberService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final MemberService memberService;

    public void withdraw(WithdrawalCommand command) {
        memberService.withdraw(command.memberId());
    }
}
