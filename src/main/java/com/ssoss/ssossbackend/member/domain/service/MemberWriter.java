package com.ssoss.ssossbackend.member.domain.service;

import com.ssoss.ssossbackend.member.domain.contract.MemberRepository;
import com.ssoss.ssossbackend.member.domain.model.Member;
import com.ssoss.ssossbackend.member.domain.model.SocialProvider;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberWriter {

    private final MemberRepository memberRepository;

    public Member register(SocialProvider provider, String socialId, String email) {
        return memberRepository.save(Member.register(provider, socialId, email));
    }
}
