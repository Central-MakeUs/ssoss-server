package com.ssoss.ssossbackend.member.domain.service;

import java.time.Clock;

import com.ssoss.ssossbackend.member.domain.contract.MemberWithdrawalHistoryRepository;
import com.ssoss.ssossbackend.member.domain.model.MemberWithdrawalHistory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberWithdrawalHistoryCleaner {

    private final MemberWithdrawalHistoryRepository memberWithdrawalHistoryRepository;
    private final Clock clock;

    public int clean() {
        return memberWithdrawalHistoryRepository.deleteAllByWithdrawnAtBefore(
            clock.instant().minus(MemberWithdrawalHistory.SIGNUP_RESTRICTION_PERIOD));
    }
}
