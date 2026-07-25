package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

record AppleTokenResponse(@JsonProperty("refresh_token") String refreshToken) {
}
