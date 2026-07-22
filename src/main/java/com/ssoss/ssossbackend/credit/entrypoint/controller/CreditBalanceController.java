package com.ssoss.ssossbackend.credit.entrypoint.controller;

import com.ssoss.ssossbackend.credit.application.service.CreditBalanceResult;
import com.ssoss.ssossbackend.credit.application.service.CreditService;
import com.ssoss.ssossbackend.credit.entrypoint.response.CreditBalanceResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class CreditBalanceController implements CreditBalanceApi {

    private final CreditService creditService;

    @Override
    @GetMapping("/v1/credits/me")
    public CreditBalanceResponse getBalance(@AuthenticationPrincipal Long memberId) {
        CreditBalanceResult result = creditService.getBalance(memberId);
        return new CreditBalanceResponse(result.balance());
    }
}
