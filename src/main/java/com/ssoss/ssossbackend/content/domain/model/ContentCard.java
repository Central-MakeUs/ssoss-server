package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

public record ContentCard(Content content, List<Channel> channels, ContentChannel representative,
    List<String> hashtags) {

    public static ContentCard of(Content content, List<ContentChannelView> orderedViews,
        ContentChannel representative) {
        return new ContentCard(content,
            orderedViews.stream().map(ContentChannelView::channel).toList(),
            representative,
            orderedViews.stream()
                .map(ContentChannelView::hashtagList)
                .filter(hashtags -> !hashtags.isEmpty())
                .findFirst()
                .orElse(List.of()));
    }
}
