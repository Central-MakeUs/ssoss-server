package com.ssoss.ssossbackend.auth.domain.service;

import com.ssoss.ssossbackend.auth.domain.contract.TokenGenerator;
import com.ssoss.ssossbackend.auth.domain.model.LoginToken;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenIssuer {

    private final TokenGenerator tokenGenerator;
    private final RefreshTokenWriter refreshTokenWriter;

    public LoginToken issue(Long memberId) {
        LoginToken loginToken = tokenGenerator.generate(memberId);
        refreshTokenWriter.replace(memberId, loginToken.refreshToken());
        return loginToken;
    }
}
