package com.ssoss.ssossbackend.auth.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답 — 회전된 새 토큰 쌍")
public record TokenRefreshResponse(
    @Schema(description = "API 인증용 액세스 토큰 (JWT, 기본 만료 30분)",
        example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIn0.x")
    String accessToken,
    @Schema(description = "액세스 토큰 재발급용 리프레시 토큰 (opaque 랜덤 문자열, 기본 만료 14일, 재발급 시 회전)",
        example = "3q2nq0uW9kZ0m1r5c8vX2yB7dF4hJ6lN8pR0tV2xZ4A")
    String refreshToken
) {
}
