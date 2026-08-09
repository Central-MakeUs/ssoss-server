package com.ssoss.ssossbackend.content.application.result;

import java.time.Instant;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentCard;
import com.ssoss.ssossbackend.content.domain.model.ContentName;

public record ContentSummaryResult(Long contentId, String name, Instant savedAt, List<String> channels, String purpose,
                                   String tone, String title, List<String> hashtags) {

    private static final int HASHTAG_LIMIT = 2;

    public static ContentSummaryResult from(ContentCard card) {
        Content content = card.content();
        return new ContentSummaryResult(
            content.getId(),
            content.getName(),
            content.getCreatedAt(),
            card.channels().stream().map(Channel::name).toList(),
            content.getPurpose().name(),
            content.getTone().name(),
            ContentName.of(card.representative()).value(),
            card.hashtags().stream().limit(HASHTAG_LIMIT).toList());
    }
}
