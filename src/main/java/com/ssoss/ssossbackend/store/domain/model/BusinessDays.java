package com.ssoss.ssossbackend.store.domain.model;

import java.time.DayOfWeek;
import java.util.List;

public record BusinessDays(List<DayOfWeek> values) {

    public BusinessDays {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
