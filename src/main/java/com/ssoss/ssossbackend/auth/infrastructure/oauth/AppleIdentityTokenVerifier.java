package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import com.ssoss.ssossbackend.auth.domain.model.AuthErrorCode;
import com.ssoss.ssossbackend.auth.domain.model.SocialProfile;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class AppleIdentityTokenVerifier {

    private static final String ISSUER = "https://appleid.apple.com";

    private final AppleSigningKeyLocator appleSigningKeyLocator;
    private final String clientId;

    AppleIdentityTokenVerifier(
        AppleSigningKeyLocator appleSigningKeyLocator,
        @Value("${auth.oauth.apple.client-id}") String clientId
    ) {
        this.appleSigningKeyLocator = appleSigningKeyLocator;
        this.clientId = clientId;
    }

    SocialProfile verify(String identityToken) {
        try {
            Claims claims = Jwts.parser()
                .keyLocator(appleSigningKeyLocator)
                .requireIssuer(ISSUER)
                .requireAudience(clientId)
                .build()
                .parseSignedClaims(identityToken)
                .getPayload();
            return new SocialProfile(claims.getSubject(), claims.get("email", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }
    }
}
