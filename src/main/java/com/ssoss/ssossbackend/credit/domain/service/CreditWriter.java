package com.ssoss.ssossbackend.credit.domain.service;

import com.ssoss.ssossbackend.credit.domain.contract.CreditLedgerRepository;
import com.ssoss.ssossbackend.credit.domain.contract.CreditRepository;
import com.ssoss.ssossbackend.credit.domain.model.Credit;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditWriter {

    private final CreditRepository creditRepository;
    private final CreditLedgerRepository creditLedgerRepository;

    @Transactional
    public void grant(Long memberId) {
        creditRepository.save(Credit.create(memberId).grant(Credit.CYCLE_FREE_GRANT));
        creditLedgerRepository.save(CreditLedger.grant(memberId, Credit.CYCLE_FREE_GRANT));
    }

    @Transactional
    public void deleteAllByMemberId(Long memberId) {
        creditLedgerRepository.deleteAllByMemberId(memberId);
        creditRepository.deleteByMemberId(memberId);
    }
}
