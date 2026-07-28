package com.ssoss.ssossbackend.store.application.result;

import com.ssoss.ssossbackend.store.domain.model.Store;

public record StoreBasicInfoResult(
    String name,
    String type,
    String address,
    String introduction,
    String status
) {

    public static StoreBasicInfoResult from(Store store) {
        return new StoreBasicInfoResult(
            store.getName(),
            store.typeName(),
            store.getAddress(),
            store.getIntroduction(),
            store.basicInfoStatus().name());
    }
}
