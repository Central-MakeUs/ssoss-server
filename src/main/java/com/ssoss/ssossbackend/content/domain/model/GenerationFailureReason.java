package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public enum GenerationFailureReason {

    OVERLOADED,
    TIMED_OUT,
    EMPTY_OUTPUT;

    public static Optional<GenerationFailureReason> derive(List<GenerationResult> results) {
        List<GenerationFailureReason> reasons = results.stream()
            .map(result -> from(result.getStatus()))
            .flatMap(Optional::stream)
            .toList();
        return Stream.of(values())
            .filter(reasons::contains)
            .findFirst();
    }

    public static Optional<GenerationFailureReason> from(GenerationResultStatus status) {
        return switch (status) {
            case RATE_LIMITED, SERVER_ERROR, CONNECTION_ERROR -> Optional.of(OVERLOADED);
            case TIMEOUT, DISCARDED_LATE -> Optional.of(TIMED_OUT);
            case EMPTY_OUTPUT -> Optional.of(GenerationFailureReason.EMPTY_OUTPUT);
            case SUCCEEDED -> Optional.empty();
        };
    }
}
