package com.ssoss.ssossbackend.support;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Jwks;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class TestAppleApi {

    public static final String CLIENT_ID = "test-apple-client-id";

    private static final String ISSUER = "https://appleid.apple.com";
    private static final String KEY_ID = "test-key-id";
    private static final String DEFAULT_EMAIL = "test@icloud.com";

    private final MockWebServer server = new MockWebServer();
    private KeyPair keyPair;
    private KeyPair unknownKeyPair;

    public void start() throws IOException {
        keyPair = generateRsaKeyPair();
        unknownKeyPair = generateRsaKeyPair();
        server.start();
    }

    public void shutdown() throws IOException {
        server.shutdown();
    }

    public String jwksUrl() {
        return server.url("/auth/keys").toString();
    }

    public void stubJwks() {
        String body = jwksJson();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(body);
            }
        });
    }

    public void stubMalformedJwks() {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                    .setHeader("Content-Type", "text/html")
                    .setBody("<html>Service Temporarily Unavailable</html>");
            }
        });
    }

    public void stubServerError() {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                    .setResponseCode(500)
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"message\":\"internal server error\"}");
            }
        });
    }

    public String issueIdentityToken(String sub) {
        return issueIdentityToken(sub, DEFAULT_EMAIL);
    }

    public String issueIdentityToken(String sub, String email) {
        return identityToken(sub, email, keyPair, CLIENT_ID, Date.from(Instant.now().plusSeconds(3600)));
    }

    public String issueIdentityTokenWithoutEmail(String sub) {
        return identityToken(sub, null, keyPair, CLIENT_ID, Date.from(Instant.now().plusSeconds(3600)));
    }

    public String issueIdentityTokenSignedByUnknownKey(String sub) {
        return identityToken(sub, DEFAULT_EMAIL, unknownKeyPair, CLIENT_ID, Date.from(Instant.now().plusSeconds(3600)));
    }

    public String issueIdentityTokenForOtherClient(String sub) {
        return identityToken(sub, DEFAULT_EMAIL, keyPair, "other-apple-client-id", Date.from(Instant.now().plusSeconds(3600)));
    }

    public String issueExpiredIdentityToken(String sub) {
        return identityToken(sub, DEFAULT_EMAIL, keyPair, CLIENT_ID, Date.from(Instant.now().minusSeconds(60)));
    }

    private String identityToken(String sub, String email, KeyPair signingKeyPair, String audience, Date expiration) {
        var builder = Jwts.builder()
            .header().keyId(KEY_ID).and()
            .issuer(ISSUER)
            .subject(sub)
            .audience().add(audience).and()
            .issuedAt(Date.from(Instant.now().minusSeconds(10)))
            .expiration(expiration);
        if (email != null) {
            builder.claim("email", email);
        }
        return builder
            .signWith(signingKeyPair.getPrivate(), Jwts.SIG.RS256)
            .compact();
    }

    private String jwksJson() {
        var jwk = Jwks.builder().key((RSAPublicKey) keyPair.getPublic()).id(KEY_ID).build();
        try {
            return new ObjectMapper().writeValueAsString(Map.of("keys", List.of(jwk)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
