package com.ssoss.ssossbackend.content.domain.model;

import java.time.Instant;
import java.util.List;

import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Getter
@Table("generation_result")
public class GenerationResult {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @Id
    private Long id;
    private Long generationId;
    private Channel channel;
    private GenerationResultStatus status;
    private String title;
    private String body;
    private String hashtags;
    private long responseTimeMillis;
    private Integer inputTokens;
    private Integer outputTokens;
    private String rawResponse;

    @CreatedDate
    private Instant createdAt;

    GenerationResult(Long id, Long generationId, Channel channel, GenerationResultStatus status,
        String title, String body, String hashtags, long responseTimeMillis,
        Integer inputTokens, Integer outputTokens, String rawResponse) {
        this.id = id;
        this.generationId = generationId;
        this.channel = channel;
        this.status = status;
        this.title = title;
        this.body = body;
        this.hashtags = hashtags;
        this.responseTimeMillis = responseTimeMillis;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.rawResponse = rawResponse;
    }

    public static GenerationResult succeeded(Long generationId, Channel channel, LlmCallReply reply) {
        GeneratedContent content = reply.content();
        return new GenerationResult(null, generationId, channel, GenerationResultStatus.SUCCEEDED,
            content.title(), content.body(), JSON_MAPPER.writeValueAsString(content.hashtags()),
            reply.responseTimeMillis(), reply.inputTokens(), reply.outputTokens(), reply.rawResponse());
    }

    public static GenerationResult failed(Long generationId, Channel channel, GenerationResultStatus status,
        LlmCallReply reply) {
        return new GenerationResult(null, generationId, channel, status, null, null, null,
            reply.responseTimeMillis(), reply.inputTokens(), reply.outputTokens(), reply.rawResponse());
    }

    public static GenerationResult failed(Long generationId, Channel channel, GenerationResultStatus status,
        long responseTimeMillis, Integer inputTokens, Integer outputTokens, String rawResponse) {
        return new GenerationResult(null, generationId, channel, status, null, null, null,
            responseTimeMillis, inputTokens, outputTokens, rawResponse);
    }

    public boolean isSucceeded() {
        return status == GenerationResultStatus.SUCCEEDED;
    }

    public List<String> hashtagList() {
        if (hashtags == null || hashtags.isBlank()) {
            return List.of();
        }
        return JSON_MAPPER.readValue(hashtags, new TypeReference<List<String>>() {
        });
    }
}
