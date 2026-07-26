package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

public record Hashtags(List<String> values) {

    public Hashtags {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
