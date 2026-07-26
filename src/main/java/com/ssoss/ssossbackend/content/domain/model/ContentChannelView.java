package com.ssoss.ssossbackend.content.domain.model;

import java.util.Comparator;

public record ContentChannelView(Long id, Long contentId, Channel channel) {

    public static final Comparator<ContentChannelView> CHANNEL_ORDER =
        Comparator.comparing(ContentChannelView::channel);
}
