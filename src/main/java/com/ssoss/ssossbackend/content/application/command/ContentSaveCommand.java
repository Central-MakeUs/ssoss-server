package com.ssoss.ssossbackend.content.application.command;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.ContentChannelDraft;

public record ContentSaveCommand(Long memberId, Long generationId, List<ContentChannelDraft> contents) {

    public static ContentSaveCommand of(Long memberId, Long generationId, List<Item> contents) {
        if (contents == null) {
            return new ContentSaveCommand(memberId, generationId, List.of());
        }
        return new ContentSaveCommand(memberId, generationId, contents.stream()
            .map(item -> new ContentChannelDraft(
                Channel.from(item.channel()), item.title(), item.body(), item.hashtags()))
            .toList());
    }

    public record Item(String channel, String title, String body, List<String> hashtags) {
    }
}
