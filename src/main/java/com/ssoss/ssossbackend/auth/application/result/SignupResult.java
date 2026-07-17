package com.ssoss.ssossbackend.auth.application.result;

public record SignupResult(String status, String accessToken, String refreshToken) {
}
