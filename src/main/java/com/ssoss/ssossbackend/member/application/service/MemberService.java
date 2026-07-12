package com.ssoss.ssossbackend.member.application.service;

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

    public Long findOrRegister(String provider, String socialId) {
        SocialProvider socialProvider = SocialProvider.valueOf(provider);
        return memberFinder.find(socialProvider, socialId)
            .orElseGet(() -> memberWriter.register(socialProvider, socialId))
            .getId();
    }
}
