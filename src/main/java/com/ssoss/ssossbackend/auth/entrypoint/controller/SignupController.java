package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.application.result.SignupResult;
import com.ssoss.ssossbackend.auth.application.service.SignupService;
import com.ssoss.ssossbackend.auth.entrypoint.request.SignupRequest;
import com.ssoss.ssossbackend.auth.entrypoint.response.SignupResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class SignupController implements SignupApi {

    private final SignupService signupService;

    @Override
    @PostMapping("/v1/signup")
    public SignupResponse signup(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody SignupRequest request
    ) {
        SignupResult result = signupService.signup(request.toCommand(memberId));
        return new SignupResponse(result.status(), result.accessToken(), result.refreshToken());
    }
}
