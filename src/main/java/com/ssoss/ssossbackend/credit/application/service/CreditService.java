package com.ssoss.ssossbackend.credit.application.service;

import com.ssoss.ssossbackend.credit.domain.service.CreditBalanceReader;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditBalanceReader creditBalanceReader;

    public CreditBalanceResult readBalance(Long memberId) {
        return CreditBalanceResult.from(creditBalanceReader.read(memberId));
    }
}
