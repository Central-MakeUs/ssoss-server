package com.ssoss.ssossbackend.content.domain.model;

import java.util.Arrays;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.exception.CommonErrorCode;

import org.springframework.data.domain.Sort;

public enum ContentSort {

    LATEST(Sort.Direction.DESC),
    OLDEST(Sort.Direction.ASC);

    private final Sort.Direction direction;

    ContentSort(Sort.Direction direction) {
        this.direction = direction;
    }

    public static ContentSort from(String value) {
        return Arrays.stream(values())
            .filter(sort -> sort.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_INPUT));
    }

    public Sort order() {
        return Sort.by(direction, "createdAt", "id");
    }
}
