package com.ssoss.ssossbackend.member.application.result;

import java.time.Instant;

import com.ssoss.ssossbackend.member.domain.model.Member;

public record MemberIdentityResult(Long id, String status, Instant lastWithdrawnAt) {

    public static MemberIdentityResult from(Member member) {
        return new MemberIdentityResult(member.getId(), member.getStatus().name(), member.getLastWithdrawnAt());
    }
}
