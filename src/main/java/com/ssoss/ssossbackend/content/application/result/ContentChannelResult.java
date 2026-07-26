package com.ssoss.ssossbackend.content.application.result;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.ContentChannel;

public record ContentChannelResult(
    Long contentChannelId,
    String channel,
    String title,
    String body,
    List<String> hashtags
) {

    public static ContentChannelResult from(ContentChannel channel) {
        return new ContentChannelResult(channel.getId(), channel.getChannel().name(), channel.getTitle(),
            channel.getBody(), channel.hashtagList());
    }
}
