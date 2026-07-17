package com.ssoss.ssossbackend.member.application.service;

import com.ssoss.ssossbackend.member.domain.model.Member;

public record MemberIdentity(Long id, String status) {

    static MemberIdentity from(Member member) {
        return new MemberIdentity(member.getId(), member.getStatus().name());
    }
}
