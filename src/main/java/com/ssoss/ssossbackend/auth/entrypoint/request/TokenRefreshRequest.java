package com.ssoss.ssossbackend.auth.entrypoint.request;

import com.ssoss.ssossbackend.auth.application.command.TokenRefreshCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record TokenRefreshRequest(
    @Schema(description = "로그인 또는 직전 재발급으로 받은 리프레시 토큰", example = "3q2nq0uW9kZ0m1r5c8vX2yB7dF4hJ6lN8pR0tV2xZ4A",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "리프레시 토큰을 입력해 주세요")
    String refreshToken
) {

    public TokenRefreshCommand toCommand() {
        return new TokenRefreshCommand(refreshToken);
    }
}
