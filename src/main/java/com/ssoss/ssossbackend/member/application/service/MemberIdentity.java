package com.ssoss.ssossbackend.member.application.service;

import java.time.Instant;

import com.ssoss.ssossbackend.member.domain.model.Member;

public record MemberIdentity(Long id, String status, Instant lastWithdrawnAt) {

    static MemberIdentity from(Member member) {
        return new MemberIdentity(member.getId(), member.getStatus().name(), member.getLastWithdrawnAt());
    }

    public boolean hasWithdrawnSince(Instant moment) {
        return lastWithdrawnAt != null && !lastWithdrawnAt.isBefore(moment);
    }
}
