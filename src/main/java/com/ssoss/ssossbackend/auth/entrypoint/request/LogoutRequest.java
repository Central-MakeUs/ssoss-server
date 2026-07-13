package com.ssoss.ssossbackend.auth.entrypoint.request;

import com.ssoss.ssossbackend.auth.application.command.LogoutCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그아웃 요청")
public record LogoutRequest(
    @Schema(description = "폐기할 리프레시 토큰 (로그인 또는 직전 재발급으로 받은 값)", example = "3q2nq0uW9kZ0m1r5c8vX2yB7dF4hJ6lN8pR0tV2xZ4A",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "리프레시 토큰을 입력해 주세요")
    String refreshToken
) {

    public LogoutCommand toCommand() {
        return new LogoutCommand(refreshToken);
    }
}
