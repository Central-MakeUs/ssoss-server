package com.ssoss.ssossbackend.content.domain.model;

public enum GenerationResultStatus {

    SUCCEEDED,
    RATE_LIMITED,
    SERVER_ERROR,
    CONNECTION_ERROR,
    TIMEOUT,
    EMPTY_OUTPUT,
    DISCARDED_LATE
}
