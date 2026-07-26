package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

public record ChannelResult(
    Channel channel,
    String title,
    String body,
    List<String> hashtags
) {

    public static ChannelResult from(GenerationResult result) {
        return new ChannelResult(result.getChannel(), result.getTitle(), result.getBody(), result.hashtagList());
    }
}
