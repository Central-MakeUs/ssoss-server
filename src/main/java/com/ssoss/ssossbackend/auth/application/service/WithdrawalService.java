package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.WithdrawalCommand;
import com.ssoss.ssossbackend.auth.domain.service.AccountWriter;
import com.ssoss.ssossbackend.auth.domain.service.SocialUnlinker;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final AccountWriter accountWriter;
    private final SocialUnlinker socialUnlinker;

    public void withdraw(WithdrawalCommand command) {
        accountWriter.withdraw(command.memberId(), command.reason(), command.detail());
        socialUnlinker.unlink(command.memberId());
    }
}
