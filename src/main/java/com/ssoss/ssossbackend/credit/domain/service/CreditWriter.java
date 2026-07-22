package com.ssoss.ssossbackend.credit.domain.service;

import java.time.Clock;

import com.ssoss.ssossbackend.credit.domain.contract.CreditLedgerRepository;
import com.ssoss.ssossbackend.credit.domain.contract.CreditRepository;
import com.ssoss.ssossbackend.credit.domain.model.Credit;
import com.ssoss.ssossbackend.credit.domain.model.CreditCycle;
import com.ssoss.ssossbackend.credit.domain.model.CreditErrorCode;
import com.ssoss.ssossbackend.credit.domain.model.CreditLedger;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditWriter {

    private final CreditRepository creditRepository;
    private final CreditLedgerRepository creditLedgerRepository;
    private final Clock clock;

    @Transactional
    public void grant(Long memberId) {
        CreditCycle cycle = CreditCycle.current(clock.instant());
        creditRepository.save(Credit.create(memberId).grant(Credit.CYCLE_FREE_GRANT, cycle));
        creditLedgerRepository.save(CreditLedger.grant(memberId, Credit.CYCLE_FREE_GRANT));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean renewCycle(Long memberId) {
        CreditCycle cycle = CreditCycle.current(clock.instant());
        Credit credit = creditRepository.findByMemberId(memberId)
            .orElseThrow(() -> new BusinessException(CreditErrorCode.CREDIT_NOT_FOUND));
        if (credit.isGrantedFor(cycle)) {
            return false;
        }
        int expired = credit.expireFree();
        if (expired > 0) {
            creditLedgerRepository.save(CreditLedger.expire(memberId, expired));
        }
        creditRepository.save(credit.grant(Credit.CYCLE_FREE_GRANT, cycle));
        creditLedgerRepository.save(CreditLedger.grant(memberId, Credit.CYCLE_FREE_GRANT));
        return true;
    }

    @Transactional
    public void deleteAllByMemberId(Long memberId) {
        creditLedgerRepository.deleteAllByMemberId(memberId);
        creditRepository.deleteByMemberId(memberId);
    }
}
