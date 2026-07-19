package com.ssoss.ssossbackend.auth.domain.service;

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenCleaner {

    private static final Duration RETENTION_AFTER_EXPIRY = Duration.ofDays(30);

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public int clean() {
        return refreshTokenRepository.deleteAllByExpiresAtBefore(clock.instant().minus(RETENTION_AFTER_EXPIRY));
    }

    public int deleteAllByMemberIds(Collection<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return 0;
        }
        return refreshTokenRepository.deleteAllByMemberIdIn(memberIds);
    }
}
