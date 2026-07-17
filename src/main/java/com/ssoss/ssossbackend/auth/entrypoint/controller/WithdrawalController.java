package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.application.command.WithdrawalCommand;
import com.ssoss.ssossbackend.auth.application.service.WithdrawalService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class WithdrawalController implements WithdrawalApi {

    private final WithdrawalService withdrawalService;

    @Override
    @DeleteMapping("/v1/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal Long memberId) {
        withdrawalService.withdraw(new WithdrawalCommand(memberId));
    }
}
