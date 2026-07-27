package com.ssoss.ssossbackend.store.domain.model;

import java.util.List;

public record SignatureMenus(List<String> values) {

    public SignatureMenus {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
