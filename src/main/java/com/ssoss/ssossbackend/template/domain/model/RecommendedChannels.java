package com.ssoss.ssossbackend.template.domain.model;

import java.util.List;

public record RecommendedChannels(List<Channel> values) {

    public RecommendedChannels {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
