package com.ssoss.ssossbackend.member.application.service;

import java.util.List;
import java.util.Optional;

import com.ssoss.ssossbackend.member.application.event.MemberActivatedEvent;
import com.ssoss.ssossbackend.member.application.event.MemberDeletedEvent;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberTerm;
import com.ssoss.ssossbackend.member.domain.model.SocialProvider;
import com.ssoss.ssossbackend.member.domain.service.MemberFinder;
import com.ssoss.ssossbackend.member.domain.service.MemberTermWriter;
import com.ssoss.ssossbackend.member.domain.service.MemberWithdrawalHistoryCleaner;
import com.ssoss.ssossbackend.member.domain.service.MemberWriter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    public MemberIdentity signup(Long memberId, boolean serviceTermsAgreed, boolean privacyPolicyAgreed,
        boolean marketingAgreed) {
        Member member = memberWriter.activate(memberId);
        memberTermWriter.record(MemberTerm.record(
            member.getId(), serviceTermsAgreed, privacyPolicyAgreed, marketingAgreed));
        eventPublisher.publishEvent(new MemberActivatedEvent(member.getId()));
        return MemberIdentity.from(member);
    }

    public void withdraw(Long memberId) {
        memberWriter.withdraw(memberId);
    }

    public MemberIdentity recover(Long memberId) {
        return MemberIdentity.from(memberWriter.recover(memberId));
    }

    public List<Long> findAllDueForDeletion() {
        return memberWriter.findAllDueForDeletion();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteWithdrawn(Long memberId) {
        if (!memberWriter.deleteWithdrawn(memberId)) {
            return;
        }
        memberTermWriter.deleteAllByMemberId(memberId);
        eventPublisher.publishEvent(new MemberDeletedEvent(memberId));
    }

    public int cleanUpWithdrawalHistories() {
        return memberWithdrawalHistoryCleaner.clean();
    }
}
