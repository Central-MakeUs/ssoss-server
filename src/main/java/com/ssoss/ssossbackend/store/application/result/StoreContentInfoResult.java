package com.ssoss.ssossbackend.store.application.result;

import java.util.List;

import com.ssoss.ssossbackend.store.domain.model.Store;

public record StoreContentInfoResult(
    String strength,
    List<String> keywords,
    String forbidden,
    String tone,
    String status
) {

    public static StoreContentInfoResult from(Store store) {
        return new StoreContentInfoResult(
            store.getStrength(),
            store.keywordValues(),
            store.getForbidden(),
            store.toneName(),
            store.contentInfoStatus().name());
    }
}
