package com.ssoss.ssossbackend.content.application.result;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentWithChannels;

public record ContentDetailResult(
    Long contentId,
    String name,
    String purpose,
    String tone,
    List<String> keywords,
    List<ContentChannelResult> contents
) {

    public static ContentDetailResult from(ContentWithChannels found) {
        Content content = found.content();
        return new ContentDetailResult(
            content.getId(),
            content.getName(),
            content.getPurpose().name(),
            content.getTone().name(),
            content.keywordList(),
            found.channels().stream()
                .map(ContentChannelResult::from)
                .toList());
    }
}
