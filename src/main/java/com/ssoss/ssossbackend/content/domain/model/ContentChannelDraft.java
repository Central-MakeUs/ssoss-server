package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

import org.springframework.util.StringUtils;

public record ContentChannelDraft(Channel channel, String title, String body, List<String> hashtags) {

    public ContentChannelDraft {
        title = StringUtils.hasText(title) ? title : null;
        hashtags = hashtags == null ? List.of() : List.copyOf(hashtags);
    }
}
