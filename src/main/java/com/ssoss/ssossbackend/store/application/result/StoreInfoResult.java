package com.ssoss.ssossbackend.store.application.result;

import com.ssoss.ssossbackend.store.domain.model.Store;

public record StoreInfoResult(
    StoreBasicInfoResult basic,
    StoreOperationInfoResult operation,
    StoreContentInfoResult content
) {

    public static StoreInfoResult from(Store store) {
        return new StoreInfoResult(
            StoreBasicInfoResult.from(store),
            StoreOperationInfoResult.from(store),
            StoreContentInfoResult.from(store));
    }
}
