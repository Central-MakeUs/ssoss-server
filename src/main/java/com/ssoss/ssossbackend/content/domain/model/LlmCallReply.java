package com.ssoss.ssossbackend.content.domain.model;

public record LlmCallReply(
    GeneratedContent content,
    long responseTimeMillis,
    Integer inputTokens,
    Integer outputTokens,
    String rawResponse
) {
}
