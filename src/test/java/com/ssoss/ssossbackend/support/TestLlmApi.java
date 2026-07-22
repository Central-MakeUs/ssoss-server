package com.ssoss.ssossbackend.support;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class TestLlmApi {

    private final MockWebServer server = new MockWebServer();
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final List<String> recordedRequestBodies = new CopyOnWriteArrayList<>();
    private volatile Integer failureStatus;
    private volatile boolean emptyBody;
    private volatile boolean emptyBodyForUntitled;
    private volatile boolean malformedContent;

    public void start() throws IOException {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                String requestBody = request.getBody().readUtf8();
                recordedRequestBodies.add(requestBody);
                if (failureStatus != null) {
                    return new MockResponse()
                        .setResponseCode(failureStatus)
                        .setHeader("Content-Type", "application/json")
                        .setBody("""
                            {"error":{"code":%d,"message":"stubbed failure","status":"STUBBED"}}
                            """.formatted(failureStatus));
                }
                return new MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(generateContentBody(requestBody));
            }
        });
        server.start();
    }

    public void stubFailure(int statusCode) {
        this.failureStatus = statusCode;
    }

    public void stubEmptyBody() {
        this.emptyBody = true;
    }

    public void stubEmptyBodyForUntitled() {
        this.emptyBodyForUntitled = true;
    }

    public void stubMalformedContent() {
        this.malformedContent = true;
    }

    public void shutdown() throws IOException {
        server.shutdown();
    }

    public String baseUrl() {
        String url = server.url("/").toString();
        return url.substring(0, url.length() - 1);
    }

    public List<String> recordedRequestBodies() {
        return List.copyOf(recordedRequestBodies);
    }

    public void reset() {
        failureStatus = null;
        emptyBody = false;
        emptyBodyForUntitled = false;
        malformedContent = false;
        recordedRequestBodies.clear();
    }

    private String generateContentBody(String requestBody) {
        if (malformedContent) {
            return completionEnvelope("이건 JSON 이 아닙니다");
        }
        boolean titled = requestsTitle(mapper.readTree(requestBody));
        String body = emptyBody || (emptyBodyForUntitled && !titled) ? "" : "테스트 본문";
        Map<String, Object> content = titled
            ? Map.of("title", "테스트 제목", "body", body, "hashtags", List.of("#테스트", "#쏘쓰"))
            : Map.of("body", body, "hashtags", List.of("#테스트", "#쏘쓰"));
        return completionEnvelope(mapper.writeValueAsString(content));
    }

    private String completionEnvelope(String contentText) {
        Map<String, Object> completion = Map.of(
            "candidates", List.of(Map.of(
                "content", Map.of("role", "model", "parts", List.of(Map.of("text", contentText))),
                "finishReason", "STOP",
                "index", 0)),
            "usageMetadata", Map.of("promptTokenCount", 10, "candidatesTokenCount", 20, "totalTokenCount", 30),
            "modelVersion", "gemini-3.1-flash-lite");
        return mapper.writeValueAsString(completion);
    }

    private boolean requestsTitle(JsonNode node) {
        if (node.isObject()) {
            JsonNode properties = node.get("properties");
            if (properties != null && properties.has("title")) {
                return true;
            }
        }
        for (JsonNode child : node) {
            if (requestsTitle(child)) {
                return true;
            }
        }
        return false;
    }
}
