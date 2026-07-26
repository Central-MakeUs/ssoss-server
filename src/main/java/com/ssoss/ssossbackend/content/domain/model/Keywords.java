package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

public record Keywords(List<String> values) {

    public Keywords {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
