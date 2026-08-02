package com.ssoss.ssossbackend.content.domain.model;

import java.util.Comparator;
import java.util.List;

public record ContentChannelView(Long id, Long contentId, Channel channel, Hashtags hashtags) {

    public static final Comparator<ContentChannelView> CHANNEL_ORDER =
        Comparator.comparing(ContentChannelView::channel);

    public List<String> hashtagList() {
        return hashtags == null ? List.of() : hashtags.values();
    }
}
