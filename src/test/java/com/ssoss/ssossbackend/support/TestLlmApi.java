package com.ssoss.ssossbackend.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private volatile int markerCount = 2;
    private volatile int photoGuideCount = 2;

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

    public void stubPhotoGuides(int markerCount, int photoGuideCount) {
        this.markerCount = markerCount;
        this.photoGuideCount = photoGuideCount;
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

    public List<String> recordedOutputSchemas() {
        return recordedRequestBodies.stream()
            .map(body -> String.valueOf(outputProperties(mapper.readTree(body))))
            .toList();
    }

    public void reset() {
        failureStatus = null;
        emptyBody = false;
        emptyBodyForUntitled = false;
        malformedContent = false;
        markerCount = 2;
        photoGuideCount = 2;
        recordedRequestBodies.clear();
    }

    private String generateContentBody(String requestBody) {
        if (malformedContent) {
            return completionEnvelope("이건 JSON 이 아닙니다");
        }
        JsonNode properties = outputProperties(mapper.readTree(requestBody));
        if (properties == null) {
            throw new IllegalStateException("LLM 요청에서 출력 스키마를 찾지 못했습니다: " + requestBody);
        }
        boolean titled = properties.has("title");
        boolean photoGuided = properties.has("photoGuides");
        List<String> paragraphs = emptyBody || (emptyBodyForUntitled && !titled)
            ? List.of()
            : paragraphsWithMarkers(photoGuided);
        Map<String, Object> content = new LinkedHashMap<>();
        if (titled) {
            content.put("title", "테스트 제목");
        }
        content.put("paragraphs", paragraphs);
        if (properties.has("hashtags")) {
            content.put("hashtags", List.of("#테스트", "#쏘쓰"));
        }
        if (photoGuided) {
            content.put("photoGuides", photoGuides());
        }
        return completionEnvelope(mapper.writeValueAsString(content));
    }

    private List<String> paragraphsWithMarkers(boolean photoGuided) {
        List<String> paragraphs = new ArrayList<>();
        paragraphs.add("테스트 본문");
        if (photoGuided) {
            for (int number = 1; number <= markerCount; number++) {
                paragraphs.add("<photo-guide/>");
                paragraphs.add("이어지는 본문 %d".formatted(number));
            }
        }
        return paragraphs;
    }

    private List<Map<String, Object>> photoGuides() {
        List<Map<String, Object>> guides = new ArrayList<>();
        for (int number = 1; number <= photoGuideCount; number++) {
            guides.add(Map.of("title", "사진 제목 %d".formatted(number),
                "description", "사진 설명 %d".formatted(number)));
        }
        return guides;
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

    private JsonNode outputProperties(JsonNode node) {
        if (node.isObject()) {
            JsonNode properties = node.get("properties");
            if (properties != null && properties.has("paragraphs")) {
                return properties;
            }
        }
        for (JsonNode child : node) {
            JsonNode found = outputProperties(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
