package com.ssoss.ssossbackend.auth.domain.model;

public record LoginToken(String accessToken, String refreshToken) {
}
