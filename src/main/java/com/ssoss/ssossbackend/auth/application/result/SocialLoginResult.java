package com.ssoss.ssossbackend.auth.application.result;

public record SocialLoginResult(String status, String accessToken, String refreshToken) {
}
