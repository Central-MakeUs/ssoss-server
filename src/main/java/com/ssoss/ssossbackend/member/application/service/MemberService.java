package com.ssoss.ssossbackend.member.application.service;

import java.util.List;
import java.util.Optional;

import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberTerm;
import com.ssoss.ssossbackend.member.domain.model.SocialProvider;
import com.ssoss.ssossbackend.member.domain.model.WithdrawalReason;
import com.ssoss.ssossbackend.member.domain.service.MemberFinder;
import com.ssoss.ssossbackend.member.domain.service.MemberTermWriter;
import com.ssoss.ssossbackend.member.domain.service.MemberWithdrawalHistoryCleaner;
import com.ssoss.ssossbackend.member.domain.service.MemberWriter;
import com.ssoss.ssossbackend.member.event.MemberActivatedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberFinder memberFinder;
    private final MemberWriter memberWriter;
    private final MemberTermWriter memberTermWriter;
    private final MemberWithdrawalHistoryCleaner memberWithdrawalHistoryCleaner;
    private final ApplicationEventPublisher eventPublisher;

    public Optional<MemberIdentity> find(String provider, String socialId) {
        return memberFinder.find(SocialProvider.valueOf(provider), socialId)
            .map(MemberIdentity::from);
    }

    public Optional<MemberIdentity> findById(Long memberId) {
        return memberFinder.findById(memberId)
            .map(MemberIdentity::from);
    }

    public MemberIdentity register(String provider, String socialId, String email) {
        return MemberIdentity.from(memberWriter.register(SocialProvider.valueOf(provider), socialId, email));
    }

    @Transactional
    public MemberIdentity signup(Long memberId, boolean ageOver14Agreed, boolean serviceTermsAgreed,
        boolean privacyPolicyAgreed) {
        Member member = memberWriter.activate(memberId);
        memberTermWriter.record(MemberTerm.record(
            member.getId(), ageOver14Agreed, serviceTermsAgreed, privacyPolicyAgreed));
        eventPublisher.publishEvent(new MemberActivatedEvent(member.getId()));
        return MemberIdentity.from(member);
    }

    public void withdraw(Long memberId, String reason, String detail) {
        memberWriter.withdraw(memberId, reason == null ? null : WithdrawalReason.from(reason), detail);
    }

    public MemberIdentity recover(Long memberId) {
        return MemberIdentity.from(memberWriter.recover(memberId));
    }

    public WithdrawnMemberDeletionResult deleteWithdrawnMembers() {
        List<Long> memberIds = memberWriter.findAllDueForDeletion();
        long deleted = 0;
        for (Long memberId : memberIds) {
            if (memberWriter.deleteWithdrawn(memberId)) {
                deleted++;
            }
        }
        return new WithdrawnMemberDeletionResult(memberIds.size(), deleted);
    }

    public int cleanUpWithdrawalHistories() {
        return memberWithdrawalHistoryCleaner.clean();
    }
}
