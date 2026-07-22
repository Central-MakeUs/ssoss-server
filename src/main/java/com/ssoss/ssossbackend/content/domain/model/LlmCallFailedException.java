package com.ssoss.ssossbackend.content.domain.model;

import lombok.Getter;

@Getter
public class LlmCallFailedException extends RuntimeException {

    private final GenerationResultStatus outcome;
    private final long responseTimeMillis;
    private final Integer inputTokens;
    private final Integer outputTokens;
    private final String rawResponse;

    public LlmCallFailedException(GenerationResultStatus outcome, long responseTimeMillis, Throwable cause) {
        this(outcome, responseTimeMillis, null, null, null, cause);
    }

    public LlmCallFailedException(GenerationResultStatus outcome, long responseTimeMillis,
        Integer inputTokens, Integer outputTokens, String rawResponse, Throwable cause) {
        super("LLM 호출에 실패했습니다: " + outcome, cause);
        this.outcome = outcome;
        this.responseTimeMillis = responseTimeMillis;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.rawResponse = rawResponse;
    }
}
