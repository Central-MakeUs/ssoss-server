package com.ssoss.ssossbackend.auth.entrypoint.request;

import com.ssoss.ssossbackend.auth.application.command.SocialLoginCommand;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "소셜 로그인 요청")
public record SocialLoginRequest(
    @Schema(description = "프로바이더가 발급한 액세스 토큰", example = "AAAAN...naver-access-token",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "소셜 액세스 토큰을 입력해 주세요")
    String accessToken
) {

    public SocialLoginCommand toCommand(String provider) {
        return SocialLoginCommand.of(provider, accessToken);
    }
}
