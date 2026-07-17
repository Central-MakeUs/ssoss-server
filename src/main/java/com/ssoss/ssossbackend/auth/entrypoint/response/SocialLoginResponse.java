package com.ssoss.ssossbackend.auth.entrypoint.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 응답 — 회원 상태와 서버 자체 발급 토큰")
public record SocialLoginResponse(
    @Schema(description = "회원 상태 — PENDING: 가입 대기 (회원가입 전), ACTIVE: 가입 회원, WITHDRAWN: 탈퇴 대기 (복구 유예 중)",
        allowableValues = {"PENDING", "ACTIVE", "WITHDRAWN"}, example = "ACTIVE")
    String status,
    @Schema(description = "API 인증용 액세스 토큰 (JWT, 기본 만료 30분) — role 클레임에 회원 상태를 담습니다",
        example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIn0.x")
    String accessToken,
    @Schema(description = "액세스 토큰 재발급용 리프레시 토큰 (opaque 랜덤 문자열, 기본 만료 14일, 재발급 시 회전)",
        example = "3q2nq0uW9kZ0m1r5c8vX2yB7dF4hJ6lN8pR0tV2xZ4A")
    String refreshToken
) {
}
