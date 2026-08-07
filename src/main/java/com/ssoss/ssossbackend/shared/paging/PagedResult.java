package com.ssoss.ssossbackend.shared.paging;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record PagedResult<T>(long totalCount, int page, int size, boolean hasNext, List<T> items) {

    public static <S, T> PagedResult<T> from(Page<S> found, Function<S, T> toItem) {
        return new PagedResult<>(found.getTotalElements(), found.getNumber(), found.getSize(), found.hasNext(),
            found.getContent().stream().map(toItem).toList());
    }
}
