package com.ssoss.ssossbackend.content.application.result;

import java.time.Instant;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.Content;
import com.ssoss.ssossbackend.content.domain.model.ContentCard;

import org.springframework.data.domain.Page;

public record ContentListResult(long totalCount, int page, int size, boolean hasNext, List<Item> contents) {

    public static ContentListResult from(Page<ContentCard> found) {
        return new ContentListResult(found.getTotalElements(), found.getNumber(), found.getSize(), found.hasNext(),
            found.getContent().stream().map(Item::from).toList());
    }

    public record Item(Long contentId, Instant savedAt, List<String> channels, String purpose, String tone,
        String title, List<String> hashtags) {

        private static final int HASHTAG_LIMIT = 2;

        public static Item from(ContentCard card) {
            Content content = card.content();
            return new Item(
                content.getId(),
                content.getCreatedAt(),
                card.channels().stream().map(Channel::name).toList(),
                content.getPurpose().name(),
                content.getTone().name(),
                ContentListTitle.of(card.representative()).value(),
                card.representative().hashtagList().stream().limit(HASHTAG_LIMIT).toList());
        }
    }
}
