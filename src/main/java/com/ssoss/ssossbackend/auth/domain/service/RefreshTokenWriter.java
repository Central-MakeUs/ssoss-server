package com.ssoss.ssossbackend.auth.domain.service;

import java.time.Instant;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenWriter {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;

    public void issue(Long memberId, String refreshToken, Instant expiresAt) {
        String tokenHash = tokenHasher.hash(refreshToken);
        refreshTokenRepository.save(RefreshToken.issue(memberId, tokenHash, expiresAt));
    }
}
