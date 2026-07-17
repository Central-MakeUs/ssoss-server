package com.ssoss.ssossbackend.member.domain.service;

import java.time.Clock;

import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberWithdrawalHistoryRepository;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberErrorCode;
import com.ssoss.ssossbackend.member.domain.model.MemberWithdrawalHistory;
import com.ssoss.ssossbackend.member.domain.model.SocialProvider;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberWriter {

    private final MemberRepository memberRepository;
    private final MemberWithdrawalHistoryRepository memberWithdrawalHistoryRepository;
    private final Clock clock;

    public Member register(SocialProvider provider, String socialId, String email) {
        return memberRepository.save(Member.register(provider, socialId, email));
    }

    public Member activate(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.activate();
        try {
            return memberRepository.save(member);
        } catch (OptimisticLockingFailureException raced) {
            log.info("회원가입 동시 요청 경합: memberId={}", memberId);
            throw new BusinessException(MemberErrorCode.ALREADY_SIGNED_UP, raced);
        }
    }

    @Transactional
    public Member withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.withdraw(clock.instant());
        try {
            member = memberRepository.save(member);
        } catch (OptimisticLockingFailureException raced) {
            log.info("탈퇴 동시 요청 경합: memberId={}", memberId);
            throw new BusinessException(MemberErrorCode.ALREADY_WITHDRAWN, raced);
        }
        memberWithdrawalHistoryRepository.save(MemberWithdrawalHistory.record(
            member.getProvider(), member.getSocialId(), member.getWithdrawnAt()));
        return member;
    }
}
