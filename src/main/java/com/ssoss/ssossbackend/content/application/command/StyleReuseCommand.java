package com.ssoss.ssossbackend.content.application.command;

import java.util.List;

public record StyleReuseCommand(
    Long memberId,
    Long contentId,
    Long contentChannelId,
    String emphasis,
    String forbidden,
    List<String> keywords,
    boolean photoGuideChecked
) {

    public StyleReuseCommand {
        keywords = keywords == null ? List.of() : List.copyOf(keywords);
    }
}
