package com.ssoss.ssossbackend.support;

import java.io.IOException;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class TestNaverApi {

    private final MockWebServer server = new MockWebServer();

    public void start() throws IOException {
        server.start();
    }

    public void shutdown() throws IOException {
        server.shutdown();
    }

    public String profileUrl() {
        return server.url("/v1/nid/me").toString();
    }

    public void stubMalformedProfile(String acceptedToken) {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (("Bearer " + acceptedToken).equals(request.getHeader("Authorization"))) {
                    return new MockResponse()
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"resultcode\":\"00\",\"message\":\"success\"}");
                }
                return new MockResponse().setResponseCode(401);
            }
        });
    }

    public void stubProfileWithoutEmail(String acceptedToken, String socialId) {
        stubProfile(acceptedToken, socialId, null);
    }

    public void stubServerError() {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse()
                    .setResponseCode(500)
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"resultcode\":\"99\",\"message\":\"internal server error\"}");
            }
        });
    }

    public void stubProfile(String acceptedToken, String socialId) {
        stubProfile(acceptedToken, socialId, "test@naver.com");
    }

    public void stubProfile(String acceptedToken, String socialId, String email) {
        String profile = email == null
                ? "{\"id\":\"%s\",\"nickname\":\"테스트\"}".formatted(socialId)
                : "{\"id\":\"%s\",\"nickname\":\"테스트\",\"email\":\"%s\"}".formatted(socialId, email);
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
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
            }
        });
    }
}
