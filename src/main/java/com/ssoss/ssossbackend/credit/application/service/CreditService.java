package com.ssoss.ssossbackend.credit.application.service;

import java.util.List;

import com.ssoss.ssossbackend.credit.domain.model.Credit;
import com.ssoss.ssossbackend.credit.domain.model.CreditErrorCode;
import com.ssoss.ssossbackend.credit.domain.service.CreditFinder;
import com.ssoss.ssossbackend.credit.domain.service.CreditWriter;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
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

    public void checkDeductible(Long memberId, int channelCount) {
        Credit credit = creditFinder.find(memberId)
            .orElseThrow(() -> new BusinessException(CreditErrorCode.CREDIT_NOT_FOUND));
        if (!credit.canAfford(Credit.RESULT_DEDUCTION * channelCount)) {
            throw new BusinessException(CreditErrorCode.CREDIT_INSUFFICIENT);
        }
    }

    public void deduct(Long memberId, Long generationResultId) {
        creditWriter.deduct(memberId, generationResultId);
    }

    public void grant(Long memberId) {
        creditWriter.grant(memberId);
    }

    public CreditCycleRenewalResult renewCycles() {
        List<Long> memberIds = creditFinder.findAllMemberIds();
        long renewed = 0;
        for (Long memberId : memberIds) {
            try {
                if (creditWriter.renewCycle(memberId)) {
                    renewed++;
                }
            } catch (Exception e) {
                log.error("크레딧 사이클 갱신에 실패해 해당 회원을 건너뜁니다: memberId={}", memberId, e);
            }
        }
        return new CreditCycleRenewalResult(memberIds.size(), renewed);
    }

    public void deleteAllByMemberId(Long memberId) {
        creditWriter.deleteAllByMemberId(memberId);
    }
}
