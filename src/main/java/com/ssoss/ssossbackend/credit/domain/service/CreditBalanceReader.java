package com.ssoss.ssossbackend.credit.domain.service;

import java.time.Clock;

import com.ssoss.ssossbackend.credit.domain.contract.CreditLedgerRepository;
import com.ssoss.ssossbackend.credit.domain.model.CreditBalance;
import com.ssoss.ssossbackend.credit.domain.model.CreditCycle;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditBalanceReader {

    private final CreditLedgerRepository creditLedgerRepository;
    private final Clock clock;

    public CreditBalance read(Long memberId) {
        CreditCycle cycle = CreditCycle.current(clock.instant());
        return CreditBalance.of(creditLedgerRepository.findAllByMemberIdAndCreatedAtGreaterThanEqual(
            memberId, cycle.startsAt()));
    }
}
