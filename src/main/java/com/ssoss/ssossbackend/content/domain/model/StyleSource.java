package com.ssoss.ssossbackend.content.domain.model;

import org.springframework.util.StringUtils;

public record StyleSource(String title, String body) {

    private static final StyleSource NONE = new StyleSource(null, null);

    public static StyleSource none() {
        return NONE;
    }

    public static StyleSource of(ContentChannel channel) {
        return new StyleSource(channel.getTitle(), PhotoGuideTag.removeFrom(channel.getBody()));
    }

    public boolean isEmpty() {
        return !StringUtils.hasText(body);
    }
}
