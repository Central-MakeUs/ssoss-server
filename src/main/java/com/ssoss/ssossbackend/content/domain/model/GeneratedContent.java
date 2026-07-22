package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

public record GeneratedContent(String title, String body, List<String> hashtags) {

    public GeneratedContent {
        hashtags = hashtags == null ? List.of() : List.copyOf(hashtags);
    }

    public boolean hasRequiredOutput(Channel channel) {
        if (body == null || body.isBlank()) {
            return false;
        }
        return !channel.hasTitle() || (title != null && !title.isBlank());
    }
}
