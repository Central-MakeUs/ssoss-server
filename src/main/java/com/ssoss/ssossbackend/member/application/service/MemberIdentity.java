package com.ssoss.ssossbackend.member.application.service;

import java.time.Instant;

import com.ssoss.ssossbackend.member.domain.model.Member;

public record MemberIdentity(Long id, String status, Instant withdrawnAt) {

    static MemberIdentity from(Member member) {
        return new MemberIdentity(member.getId(), member.getStatus().name(), member.getWithdrawnAt());
    }

    public boolean hasWithdrawnSince(Instant moment) {
        return withdrawnAt != null && !withdrawnAt.isBefore(moment);
    }
}
