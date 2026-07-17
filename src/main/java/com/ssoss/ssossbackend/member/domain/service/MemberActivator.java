package com.ssoss.ssossbackend.member.domain.service;

import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.MemberErrorCode;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberActivator {

    private final MemberRepository memberRepository;

    public Member activate(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        try {
            return memberRepository.save(member.activate());
        } catch (OptimisticLockingFailureException raced) {
            log.info("회원가입 동시 요청 경합: memberId={}", memberId);
            throw new BusinessException(MemberErrorCode.ALREADY_SIGNED_UP, raced);
        }
    }
}
