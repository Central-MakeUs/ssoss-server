package com.ssoss.ssossbackend.auth.application.result;

public record RecoveryResult(String status, String accessToken, String refreshToken) {
}
