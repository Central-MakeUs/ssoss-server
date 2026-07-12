package com.ssoss.ssossbackend.auth.domain.service;

import java.time.Clock;
import java.time.Instant;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenValidator {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;
    private final Clock clock;

    public RefreshToken validate(String refreshToken) {
        RefreshToken current = refreshTokenRepository.findByTokenHash(tokenHasher.hash(refreshToken))
            .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));
        if (current.isDeleted()) {
            log.info("폐기된 refresh token 제출: memberId={}, sessionId={}",
                current.getMemberId(), current.getSessionId());
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        Instant now = clock.instant();
        if (current.isExpired(now)) {
            try {
                refreshTokenRepository.save(current.markDeleted(now));
            } catch (OptimisticLockingFailureException markedByCompetitor) {
                throw new BusinessException(AuthErrorCode.EXPIRED_REFRESH_TOKEN, markedByCompetitor);
            }
            throw new BusinessException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }
        return current;
    }
}
