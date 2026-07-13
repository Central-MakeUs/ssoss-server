package com.ssoss.ssossbackend.auth.application.service;

import com.ssoss.ssossbackend.auth.application.command.LogoutCommand;
import com.ssoss.ssossbackend.auth.domain.service.RefreshTokenRevoker;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenRevoker refreshTokenRevoker;

    public void logout(LogoutCommand command) {
        refreshTokenRevoker.revoke(command.refreshToken());
    }
}
