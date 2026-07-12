package com.ssoss.ssossbackend.auth.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 응답 — 서버 자체 발급 JWT")
public record SocialLoginResponse(
    @Schema(description = "API 인증용 액세스 토큰 (JWT, 기본 만료 30분)",
        example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIn0.x")
    String accessToken,
    @Schema(description = "액세스 토큰 재발급용 리프레시 토큰 (JWT, 기본 만료 14일)",
        example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIn0.y")
    String refreshToken
) {
}
