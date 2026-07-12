package com.ssoss.ssossbackend.auth.application.result;

public record TokenRefreshResult(String accessToken, String refreshToken) {
}
