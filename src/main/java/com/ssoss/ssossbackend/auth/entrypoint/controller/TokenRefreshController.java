package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.application.result.TokenRefreshResult;
import com.ssoss.ssossbackend.auth.application.service.RefreshTokenService;
import com.ssoss.ssossbackend.auth.entrypoint.request.TokenRefreshRequest;
import com.ssoss.ssossbackend.auth.entrypoint.response.TokenRefreshResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class TokenRefreshController implements TokenRefreshApi {

    private final RefreshTokenService refreshTokenService;

    @Override
    @PostMapping("/v1/tokens")
    public TokenRefreshResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResult result = refreshTokenService.refresh(request.toCommand());
        return new TokenRefreshResponse(result.accessToken(), result.refreshToken());
    }
}
