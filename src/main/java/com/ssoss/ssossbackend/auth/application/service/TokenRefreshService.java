package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.TokenRefreshCommand;
import com.ssoss.ssossbackend.auth.application.result.TokenRefreshResult;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.auth.domain.service.RefreshTokenRotator;
import com.ssoss.ssossbackend.auth.domain.service.RefreshTokenValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final RefreshTokenValidator refreshTokenValidator;
    private final RefreshTokenRotator refreshTokenRotator;

    public TokenRefreshResult refresh(TokenRefreshCommand command) {
        RefreshToken current = refreshTokenValidator.validate(command.refreshToken());
        LoginToken loginToken = refreshTokenRotator.rotate(current);
        return new TokenRefreshResult(loginToken.accessToken(), loginToken.refreshToken());
    }
}
