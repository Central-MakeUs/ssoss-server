package com.ssoss.ssossbackend.auth.domain.service;

import com.ssoss.ssossbackend.auth.domain.contract.TokenGenerator;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;
import com.ssoss.ssossbackend.auth.domain.model.MemberStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenIssuer {

    private final TokenGenerator tokenGenerator;
    private final RefreshTokenWriter refreshTokenWriter;

    public LoginToken issue(Long memberId, MemberStatus status) {
        LoginToken loginToken = tokenGenerator.generate(memberId, status);
        refreshTokenWriter.issue(memberId, loginToken.refreshToken(), loginToken.refreshTokenExpiresAt());
        return loginToken;
    }
}
