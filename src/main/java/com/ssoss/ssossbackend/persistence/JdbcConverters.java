package com.ssoss.ssossbackend.persistence;

import java.util.List;

public record JdbcConverters(List<?> values) {

    public JdbcConverters {
        values = List.copyOf(values);
    }
}
