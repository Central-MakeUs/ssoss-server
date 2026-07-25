package com.ssoss.ssossbackend.support;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

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
    private static final String JWKS_PATH = "/auth/keys";
    private static final String TOKEN_PATH = "/auth/token";
    private static final String REVOKE_PATH = "/auth/revoke";

    private final MockWebServer server = new MockWebServer();
    private final List<String> tokenRequestBodies = new CopyOnWriteArrayList<>();
    private final List<String> revokeRequestBodies = new CopyOnWriteArrayList<>();

    private KeyPair keyPair;
    private KeyPair unknownKeyPair;
    private KeyPair clientSecretKeyPair;
    private volatile Function<RecordedRequest, MockResponse> jwksHandler =
        request -> new MockResponse().setResponseCode(404);
    private volatile String exchangedRefreshToken = "apple-refresh-token";
    private volatile int tokenResponseCode = 200;
    private volatile int revokeResponseCode = 200;

    public void start() throws IOException {
        keyPair = generateRsaKeyPair();
        unknownKeyPair = generateRsaKeyPair();
        clientSecretKeyPair = generateEcKeyPair();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath() == null ? "" : request.getPath();
                if (path.startsWith(TOKEN_PATH)) {
                    tokenRequestBodies.add(request.getBody().readUtf8());
                    if (tokenResponseCode != 200) {
                        return new MockResponse().setResponseCode(tokenResponseCode)
                            .setHeader("Content-Type", "application/json")
                            .setBody("{\"error\":\"server_error\"}");
                    }
                    return new MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                            {"access_token":"apple-access-token","token_type":"Bearer","expires_in":3600,\
                            "refresh_token":"%s"}""".formatted(exchangedRefreshToken));
                }
                if (path.startsWith(REVOKE_PATH)) {
                    revokeRequestBodies.add(request.getBody().readUtf8());
                    return new MockResponse().setResponseCode(revokeResponseCode);
                }
                return jwksHandler.apply(request);
            }
        });
        server.start();
    }

    public void shutdown() throws IOException {
        server.shutdown();
    }

    public void reset() {
        tokenRequestBodies.clear();
        revokeRequestBodies.clear();
        exchangedRefreshToken = "apple-refresh-token";
        tokenResponseCode = 200;
        revokeResponseCode = 200;
    }

    public String jwksUrl() {
        return server.url(JWKS_PATH).toString();
    }

    public String tokenUrl() {
        return server.url(TOKEN_PATH).toString();
    }

    public String revokeUrl() {
        return server.url(REVOKE_PATH).toString();
    }

    public List<String> tokenRequestBodies() {
        return List.copyOf(tokenRequestBodies);
    }

    public List<String> revokeRequestBodies() {
        return List.copyOf(revokeRequestBodies);
    }

    public void stubTokenExchange(String refreshToken) {
        exchangedRefreshToken = refreshToken;
        tokenResponseCode = 200;
    }

    public void stubTokenExchangeServerError() {
        tokenResponseCode = 500;
    }

    public void stubRevokeServerError() {
        revokeResponseCode = 500;
    }

    public void stubJwks() {
        String body = jwksJson();
        jwksHandler = request -> new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody(body);
    }

    public void stubMalformedJwks() {
        jwksHandler = request -> new MockResponse()
            .setHeader("Content-Type", "text/html")
            .setBody("<html>Service Temporarily Unavailable</html>");
    }

    public void stubServerError() {
        jwksHandler = request -> new MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"internal server error\"}");
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

    public String clientSecretPrivateKey() {
        return Base64.getEncoder().encodeToString(clientSecretKeyPair.getPrivate().getEncoded());
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

    private static KeyPair generateEcKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        }
    }
}
