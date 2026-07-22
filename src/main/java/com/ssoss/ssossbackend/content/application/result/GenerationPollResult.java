package com.ssoss.ssossbackend.content.application.result;

import java.util.List;

public record GenerationPollResult(
    Long generationId,
    String status,
    String failureReason,
    List<ChannelResult> results,
    List<String> pendingChannels
) {

    public record ChannelResult(String channel, String title, String body, List<String> hashtags) {
    }
}
