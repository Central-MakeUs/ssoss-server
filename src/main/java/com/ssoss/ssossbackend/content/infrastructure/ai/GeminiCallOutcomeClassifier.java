package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.io.InterruptedIOException;

import com.google.genai.errors.ApiException;
import com.ssoss.ssossbackend.content.domain.model.GenerationResultStatus;

import org.springframework.stereotype.Component;

@Component
class GeminiCallOutcomeClassifier {

    GenerationResultStatus classify(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException api) {
                if (api.code() == 429) {
                    return GenerationResultStatus.RATE_LIMITED;
                }
                if (api.code() >= 500) {
                    return GenerationResultStatus.SERVER_ERROR;
                }
            }
            if (cause instanceof InterruptedIOException) {
                return GenerationResultStatus.TIMEOUT;
            }
        }
        return GenerationResultStatus.CONNECTION_ERROR;
    }
}
