package com.ssoss.ssossbackend.auth.domain.service;

import java.time.Clock;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenExpirer {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    @Transactional
    public int expire() {
        return refreshTokenRepository.expireAll(clock.instant());
    }
}
