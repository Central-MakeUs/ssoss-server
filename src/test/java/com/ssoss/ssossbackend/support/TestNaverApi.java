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
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                if (("Bearer " + acceptedToken).equals(request.getHeader("Authorization"))) {
                    return new MockResponse()
                            .setHeader("Content-Type", "application/json")
                            .setBody("""
                                    {"resultcode":"00","message":"success",
                                     "response":{"id":"%s","nickname":"테스트","email":"test@naver.com"}}
                                    """.formatted(socialId));
                }
                return new MockResponse()
                        .setResponseCode(401)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"resultcode\":\"024\",\"message\":\"Authentication failed\"}");
            }
        });
    }
}
