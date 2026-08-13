package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.TokenRefreshCommand;
import com.ssoss.ssossbackend.auth.application.result.TokenRefreshResult;
import com.ssoss.ssossbackend.auth.domain.model.Account;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.auth.domain.service.AccountFinder;
import com.ssoss.ssossbackend.auth.domain.service.RefreshTokenCleaner;
import com.ssoss.ssossbackend.auth.domain.service.RefreshTokenRotator;
import com.ssoss.ssossbackend.auth.domain.service.RefreshTokenValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenValidator refreshTokenValidator;
    private final RefreshTokenRotator refreshTokenRotator;
    private final RefreshTokenCleaner refreshTokenCleaner;
    private final AccountFinder accountFinder;

    public TokenRefreshResult refresh(TokenRefreshCommand command) {
        RefreshToken current = refreshTokenValidator.validate(command.refreshToken());
        Account account = accountFinder.getOwnerOf(current);
        LoginToken loginToken = refreshTokenRotator.rotate(current, account.status());
        return new TokenRefreshResult(loginToken.accessToken(), loginToken.refreshToken());
    }

    public int clean() {
        return refreshTokenCleaner.clean();
    }

    public int deleteAllByMemberId(Long memberId) {
        return refreshTokenCleaner.deleteAllByMemberId(memberId);
    }
}
