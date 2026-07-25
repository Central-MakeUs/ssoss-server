package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

public record ChannelResult(
    Channel channel,
    ChannelStatus status,
    String message,
    String title,
    String body,
    List<String> hashtags
) {

    public static ChannelResult of(Channel channel, ChannelOutcome outcome) {
        return new ChannelResult(channel, outcome.getStatus(), outcome.getMessage(), null, null, List.of());
    }

    public static ChannelResult from(GenerationResult result) {
        ChannelOutcome outcome = ChannelOutcome.from(result.getStatus());
        return new ChannelResult(result.getChannel(), outcome.getStatus(), outcome.getMessage(),
            result.getTitle(), result.getBody(), result.hashtagList());
    }
}
