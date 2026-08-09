package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

import com.ssoss.ssossbackend.shared.exception.BusinessException;

public record ContentWithChannels(Content content, List<ContentChannel> channels) {

    public ContentChannel channelOf(Long contentChannelId) {
        return channels.stream()
            .filter(channel -> channel.getId().equals(contentChannelId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ContentErrorCode.CONTENT_CHANNEL_NOT_FOUND));
    }
}
