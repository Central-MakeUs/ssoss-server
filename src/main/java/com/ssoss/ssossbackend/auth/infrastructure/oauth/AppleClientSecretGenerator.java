package com.ssoss.ssossbackend.auth.infrastructure.oauth;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class AppleClientSecretGenerator {

    private static final String AUDIENCE = "https://appleid.apple.com";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final Clock clock;
    private final String teamId;
    private final String keyId;
    private final String clientId;
    private final PrivateKey privateKey;

    AppleClientSecretGenerator(
        Clock clock,
        @Value("${auth.oauth.apple.team-id}") String teamId,
        @Value("${auth.oauth.apple.key-id}") String keyId,
        @Value("${auth.oauth.apple.client-id}") String clientId,
        @Value("${auth.oauth.apple.private-key}") String privateKey
    ) {
        this.clock = clock;
        this.teamId = teamId;
        this.keyId = keyId;
        this.clientId = clientId;
        try {
            byte[] pkcs8 = Base64.getDecoder()
                .decode(privateKey.replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", ""));
            this.privateKey = KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("애플 client_secret 서명 키를 읽을 수 없습니다", e);
        }
    }

    String generate() {
        Instant now = clock.instant();
        return Jwts.builder()
            .header().keyId(keyId).and()
            .issuer(teamId)
            .subject(clientId)
            .audience().add(AUDIENCE).and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(TTL)))
            .signWith(privateKey, Jwts.SIG.ES256)
            .compact();
    }
}
