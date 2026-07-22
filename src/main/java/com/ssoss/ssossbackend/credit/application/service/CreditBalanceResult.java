package com.ssoss.ssossbackend.credit.application.service;

import com.ssoss.ssossbackend.credit.domain.model.CreditBalance;

public record CreditBalanceResult(int remaining, int limit) {

    static CreditBalanceResult from(CreditBalance balance) {
        return new CreditBalanceResult(balance.remaining(), balance.limit());
    }
}
