package com.ssoss.ssossbackend.content.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChannelOutcome {

    PENDING(ChannelStatus.PENDING, "생성 중입니다"),
    SUCCEEDED(ChannelStatus.SUCCEEDED, "생성에 성공했습니다"),
    OVERLOADED(ChannelStatus.FAILED, "요청이 많습니다. 잠시 후 다시 시도해 주세요"),
    TIMED_OUT(ChannelStatus.FAILED, "생성 시간이 초과되었습니다. 다시 시도해 주세요"),
    EMPTY_OUTPUT(ChannelStatus.FAILED, "결과를 만들지 못했습니다. 다시 시도해 주세요");

    private final ChannelStatus status;
    private final String message;

    public static ChannelOutcome from(GenerationResultStatus status) {
        return switch (status) {
            case GenerationResultStatus.SUCCEEDED -> SUCCEEDED;
            case GenerationResultStatus.RATE_LIMITED,
                 GenerationResultStatus.SERVER_ERROR,
                 GenerationResultStatus.CONNECTION_ERROR -> OVERLOADED;
            case GenerationResultStatus.TIMEOUT,
                 GenerationResultStatus.DISCARDED_LATE -> TIMED_OUT;
            case GenerationResultStatus.EMPTY_OUTPUT -> EMPTY_OUTPUT;
        };
    }
}
