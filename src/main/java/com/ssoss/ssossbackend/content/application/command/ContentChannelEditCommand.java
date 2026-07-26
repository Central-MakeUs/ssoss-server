package com.ssoss.ssossbackend.content.application.command;

import java.util.List;

public record ContentChannelEditCommand(
    Long memberId,
    Long contentId,
    Long contentChannelId,
    String title,
    String body,
    List<String> hashtags
) {

    public ContentChannelEditCommand {
        hashtags = hashtags == null ? List.of() : List.copyOf(hashtags);
    }
}
