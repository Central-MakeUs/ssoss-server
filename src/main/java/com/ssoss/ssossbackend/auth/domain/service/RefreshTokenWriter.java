package com.ssoss.ssossbackend.auth.domain.service;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenWriter {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;

    @Transactional
    public void replace(Long memberId, String refreshToken) {
        String tokenHash = tokenHasher.hash(refreshToken);
        refreshTokenRepository.findByMemberId(memberId)
            .ifPresentOrElse(
                existing -> refreshTokenRepository.save(existing.rotate(tokenHash)),
                () -> refreshTokenRepository.save(RefreshToken.issue(memberId, tokenHash))
            );
    }
}
