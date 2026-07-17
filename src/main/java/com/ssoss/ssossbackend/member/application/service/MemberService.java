package com.ssoss.ssossbackend.member.application.service;

import java.util.Optional;

import com.ssoss.ssossbackend.member.domain.model.SocialProvider;
import com.ssoss.ssossbackend.member.domain.service.MemberFinder;
import com.ssoss.ssossbackend.member.domain.service.MemberWriter;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberFinder memberFinder;
    private final MemberWriter memberWriter;

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
}
