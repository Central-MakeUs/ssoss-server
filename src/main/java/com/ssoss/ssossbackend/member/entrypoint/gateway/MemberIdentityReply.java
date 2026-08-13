package com.ssoss.ssossbackend.member.entrypoint.gateway;

import java.time.Instant;

import com.ssoss.ssossbackend.member.application.result.MemberIdentityResult;

public record MemberIdentityReply(Long id, String status, Instant lastWithdrawnAt) {

    static MemberIdentityReply from(MemberIdentityResult result) {
        return new MemberIdentityReply(result.id(), result.status(), result.lastWithdrawnAt());
    }
}
