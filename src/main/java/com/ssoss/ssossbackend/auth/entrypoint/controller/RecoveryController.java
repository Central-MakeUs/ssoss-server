package com.ssoss.ssossbackend.auth.entrypoint.controller;

import com.ssoss.ssossbackend.auth.application.command.RecoveryCommand;
import com.ssoss.ssossbackend.auth.application.result.RecoveryResult;
import com.ssoss.ssossbackend.auth.application.service.RecoveryService;
import com.ssoss.ssossbackend.auth.entrypoint.response.RecoveryResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class RecoveryController implements RecoveryApi {

    private final RecoveryService recoveryService;

    @Override
    @PostMapping("/v1/members/me/recovery")
    public RecoveryResponse recover(@AuthenticationPrincipal Long memberId) {
        RecoveryResult result = recoveryService.recover(new RecoveryCommand(memberId));
        return new RecoveryResponse(result.status(), result.accessToken(), result.refreshToken());
    }
}
