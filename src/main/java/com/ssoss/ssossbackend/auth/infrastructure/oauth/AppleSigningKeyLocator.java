package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import java.security.Key;
import java.util.Objects;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Locator;
import io.jsonwebtoken.ProtectedHeader;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AppleSigningKeyLocator implements Locator<Key> {

    private final AppleJwksHttpClient appleJwksHttpClient;

    @Override
    public Key locate(Header header) {
        if (!(header instanceof ProtectedHeader protectedHeader)) {
            return null;
        }
        JwkSet jwkSet;
        try {
            jwkSet = Jwks.setParser().build().parse(appleJwksHttpClient.fetchKeys());
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE);
        }
        return jwkSet.getKeys().stream()
            .filter(jwk -> Objects.equals(jwk.getId(), protectedHeader.getKeyId()))
            .findFirst()
            .map(Jwk::toKey)
            .orElse(null);
    }
}
