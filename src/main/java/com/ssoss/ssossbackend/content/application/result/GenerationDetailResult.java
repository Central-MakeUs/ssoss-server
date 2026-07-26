package com.ssoss.ssossbackend.content.application.result;

import java.util.List;

public record GenerationDetailResult(
    Long generationId,
    String status,
    String purpose,
    String tone,
    List<String> keywords,
    List<ChannelDetail> results
) {

    public record ChannelDetail(String channel, String title, String body, List<String> hashtags) {
    }
}
