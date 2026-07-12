package com.ssoss.ssossbackend.auth.domain.model;

import java.time.Instant;

public record LoginToken(String accessToken, String refreshToken, Instant refreshTokenExpiresAt) {
}
