package com.ssoss.ssossbackend.auth.domain.service;

import java.time.Clock;
import java.time.Instant;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenRevoker {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;
    private final Clock clock;

    public void revoke(String refreshToken) {
        Instant now = clock.instant();
        refreshTokenRepository.findByTokenHash(tokenHasher.hash(refreshToken))
            .filter(current -> !current.isDeleted())
            .filter(current -> !current.isExpired(now))
            .ifPresent(current -> refreshTokenRepository.save(current.markDeleted(now)));
    }
}
