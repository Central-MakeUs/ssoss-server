package com.ssoss.ssossbackend.credit.application.service;

import java.util.List;

import com.ssoss.ssossbackend.credit.domain.model.Credit;
import com.ssoss.ssossbackend.credit.domain.model.CreditErrorCode;
import com.ssoss.ssossbackend.credit.domain.service.CreditFinder;
import com.ssoss.ssossbackend.credit.domain.service.CreditWriter;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditFinder creditFinder;
    private final CreditWriter creditWriter;

    public CreditBalanceResult getBalance(Long memberId) {
        Credit credit = creditFinder.find(memberId)
            .orElseThrow(() -> new BusinessException(CreditErrorCode.CREDIT_NOT_FOUND));
        return new CreditBalanceResult(credit.balance());
    }

    public void grant(Long memberId) {
        creditWriter.grant(memberId);
    }

    public CreditCycleRenewalResult renewCycles() {
        List<Long> memberIds = creditFinder.findAllMemberIds();
        long renewed = memberIds.stream()
            .filter(creditWriter::renewCycle)
            .count();
        return new CreditCycleRenewalResult(memberIds.size(), renewed);
    }

    public void deleteAllByMemberId(Long memberId) {
        creditWriter.deleteAllByMemberId(memberId);
    }
}
