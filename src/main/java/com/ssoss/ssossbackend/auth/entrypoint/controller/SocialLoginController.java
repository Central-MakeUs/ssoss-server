package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.application.result.SocialLoginResult;
import com.ssoss.ssossbackend.auth.application.service.SocialLoginService;
import com.ssoss.ssossbackend.auth.entrypoint.request.SocialLoginRequest;
import com.ssoss.ssossbackend.auth.entrypoint.response.SocialLoginResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class SocialLoginController implements SocialLoginApi {

    private final SocialLoginService socialLoginService;

    @Override
    @PostMapping("/v1/social-logins/{provider}")
    public SocialLoginResponse login(
        @PathVariable String provider,
        @Valid @RequestBody SocialLoginRequest request
    ) {
        SocialLoginResult result = socialLoginService.login(request.toCommand(provider));
        return new SocialLoginResponse(result.accessToken(), result.refreshToken());
    }
}
