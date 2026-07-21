package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.TokenRefreshCommand;
import com.ssoss.ssossbackend.auth.application.result.TokenRefreshResult;
import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;
import com.ssoss.ssossbackend.auth.domain.model.RefreshToken;
import com.ssoss.ssossbackend.auth.domain.service.RefreshTokenCleaner;
import com.ssoss.ssossbackend.auth.domain.service.RefreshTokenRotator;
import com.ssoss.ssossbackend.auth.domain.service.RefreshTokenValidator;
import com.ssoss.ssossbackend.member.application.service.MemberIdentity;
import com.ssoss.ssossbackend.member.application.service.MemberService;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenValidator refreshTokenValidator;
    private final RefreshTokenRotator refreshTokenRotator;
    private final RefreshTokenCleaner refreshTokenCleaner;
    private final MemberService memberService;

    public TokenRefreshResult refresh(TokenRefreshCommand command) {
        RefreshToken current = refreshTokenValidator.validate(command.refreshToken());
        MemberIdentity member = memberService.findById(current.getMemberId())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));
        if (member.hasWithdrawnSince(current.getCreatedAt())) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        LoginToken loginToken = refreshTokenRotator.rotate(current, MemberStatus.valueOf(member.status()));
        return new TokenRefreshResult(loginToken.accessToken(), loginToken.refreshToken());
    }

    public int clean() {
        return refreshTokenCleaner.clean();
    }

    public int deleteAllByMemberId(Long memberId) {
        return refreshTokenCleaner.deleteAllByMemberId(memberId);
    }
}
