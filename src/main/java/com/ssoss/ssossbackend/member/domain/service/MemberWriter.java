package com.ssoss.ssossbackend.member.domain.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.contract.MemberWithdrawalHistoryRepository;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberErrorCode;
import com.ssoss.ssossbackend.member.domain.model.MemberStatus;
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
        if (memberWithdrawalHistoryRepository.existsByProviderAndSocialIdAndWithdrawnAtAfter(
            provider, socialId, clock.instant().minus(MemberWithdrawalHistory.SIGNUP_RESTRICTION_PERIOD))) {
            throw new BusinessException(MemberErrorCode.SIGNUP_RESTRICTED);
        }
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

    public Member recover(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.recover(clock.instant());
        try {
            return memberRepository.save(member);
        } catch (OptimisticLockingFailureException raced) {
            log.info("복구 요청 경합: memberId={}", memberId);
            throw new BusinessException(MemberErrorCode.RECOVERY_CONFLICT, raced);
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
            member.getProvider(), member.getSocialId(), member.getLastWithdrawnAt()));
        return member;
    }

    public List<Long> findAllDueForDeletion() {
        Instant threshold = clock.instant().minus(Member.RECOVERY_GRACE_PERIOD);
        return memberRepository.findAllByStatusAndLastWithdrawnAtBefore(MemberStatus.WITHDRAWN, threshold)
            .stream()
            .map(Member::getId)
            .toList();
    }

    public boolean deleteWithdrawn(Long memberId) {
        Instant threshold = clock.instant().minus(Member.RECOVERY_GRACE_PERIOD);
        int deleted = memberRepository.deleteByIdAndStatusAndLastWithdrawnAtBefore(
            memberId, MemberStatus.WITHDRAWN, threshold);
        if (deleted == 0) {
            log.info("삭제 시점에 대상이 아니어서 건너뛴 회원: memberId={}", memberId);
            return false;
        }
        log.info("복구 유예 기간이 지난 탈퇴 회원 삭제: memberId={}", memberId);
        return true;
    }
}
