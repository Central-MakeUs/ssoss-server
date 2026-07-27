package com.ssoss.ssossbackend.store.domain.model;

import java.util.List;

public record StoreKeywords(List<String> values) {

    public StoreKeywords {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
