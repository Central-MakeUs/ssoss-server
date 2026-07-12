package com.ssoss.ssossbackend.auth.domain.service;

import com.ssoss.ssossbackend.auth.domain.contract.RefreshTokenRepository;
import com.ssoss.ssossbackend.auth.domain.contract.TokenGenerator;
import com.ssoss.ssossbackend.auth.domain.contract.TokenHasher;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenRotator {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHasher tokenHasher;
    private final TokenGenerator tokenGenerator;

    @Transactional
    public LoginToken rotate(RefreshToken current) {
        try {
            refreshTokenRepository.save(current.markRotated());
        } catch (OptimisticLockingFailureException raced) {
            log.info("refresh token 동시 재발급 경합: memberId={}, sessionId={}",
                current.getMemberId(), current.getSessionId());
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN, raced);
        }
        LoginToken loginToken = tokenGenerator.generate(current.getMemberId());
        String tokenHash = tokenHasher.hash(loginToken.refreshToken());
        refreshTokenRepository.save(current.nextInSession(tokenHash, loginToken.refreshTokenExpiresAt()));
        return loginToken;
    }
}
