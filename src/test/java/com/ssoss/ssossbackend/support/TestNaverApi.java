package com.ssoss.ssossbackend.support;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class TestNaverApi {

    private static final String PROFILE_PATH = "/v1/nid/me";
    private static final String REVOKE_PATH = "/oauth2.0/revoke";

    private final MockWebServer server = new MockWebServer();
    private final List<String> revokeRequestBodies = new CopyOnWriteArrayList<>();

    private volatile Function<RecordedRequest, MockResponse> profileHandler =
        request -> new MockResponse().setResponseCode(404);
    private volatile int revokeResponseCode = 200;

    public void start() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (request.getPath() != null && request.getPath().startsWith(REVOKE_PATH)) {
                    revokeRequestBodies.add(request.getBody().readUtf8());
                    return new MockResponse()
                        .setResponseCode(revokeResponseCode)
                        .setHeader("Content-Type", "application/json")
                        .setBody(revokeResponseCode == 200 ? "{\"result\":\"success\"}" : "{\"error\":\"server_error\"}");
                }
                return profileHandler.apply(request);
            }
        });
        server.start();
    }

    public void shutdown() throws IOException {
        server.shutdown();
    }

    public void reset() {
        revokeRequestBodies.clear();
        revokeResponseCode = 200;
    }

    public String profileUrl() {
        return server.url(PROFILE_PATH).toString();
    }

    public String revokeUrl() {
        return server.url(REVOKE_PATH).toString();
    }

    public List<String> revokeRequestBodies() {
        return List.copyOf(revokeRequestBodies);
    }

    public void stubRevokeServerError() {
        revokeResponseCode = 500;
    }

    public void stubMalformedProfile(String acceptedToken) {
        profileHandler = request -> {
            if (("Bearer " + acceptedToken).equals(request.getHeader("Authorization"))) {
                return new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"resultcode\":\"00\",\"message\":\"success\"}");
            }
            return new MockResponse().setResponseCode(401);
        };
    }

    public void stubProfileWithoutEmail(String acceptedToken, String socialId) {
        stubProfile(acceptedToken, socialId, null);
    }

    public void stubServerError() {
        profileHandler = request -> new MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"resultcode\":\"99\",\"message\":\"internal server error\"}");
    }

    public void stubProfile(String acceptedToken, String socialId) {
        stubProfile(acceptedToken, socialId, "test@naver.com");
    }

    public void stubProfile(String acceptedToken, String socialId, String email) {
        String profile = email == null
                ? "{\"id\":\"%s\",\"nickname\":\"테스트\"}".formatted(socialId)
                : "{\"id\":\"%s\",\"nickname\":\"테스트\",\"email\":\"%s\"}".formatted(socialId, email);
        profileHandler = request -> {
            if (("Bearer " + acceptedToken).equals(request.getHeader("Authorization"))) {
                return new MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                                {"resultcode":"00","message":"success","response":%s}
                                """.formatted(profile));
            }
            return new MockResponse()
                    .setResponseCode(401)
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"resultcode\":\"024\",\"message\":\"Authentication failed\"}");
        };
    }
}
