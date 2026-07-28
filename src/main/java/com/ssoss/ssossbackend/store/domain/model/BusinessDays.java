package com.ssoss.ssossbackend.store.domain.model;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

public record BusinessDays(List<DayOfWeek> values) {

    public BusinessDays {
        values = values == null ? List.of() : List.copyOf(values);
    }

    public static BusinessDays from(List<String> values) {
        if (values == null) {
            return new BusinessDays(List.of());
        }
        return new BusinessDays(values.stream()
            .map(value -> Arrays.stream(DayOfWeek.values())
                .filter(day -> day.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT)))
            .distinct()
            .sorted()
            .toList());
    }
}
