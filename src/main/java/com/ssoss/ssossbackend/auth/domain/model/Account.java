package com.ssoss.ssossbackend.auth.domain.model;

import java.time.Instant;

public record Account(Long id, MemberStatus status, Instant lastWithdrawnAt) {

    public static Account of(Long id, String status, Instant lastWithdrawnAt) {
        return new Account(id, MemberStatus.valueOf(status), lastWithdrawnAt);
    }

    public boolean hasWithdrawnSince(Instant moment) {
        return lastWithdrawnAt != null && !lastWithdrawnAt.isBefore(moment);
    }
}
