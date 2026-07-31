package com.ssoss.ssossbackend.store.domain.model;

public enum StoreInfoStatus {

    NOT_WRITTEN,
    COMPLETED;

    static StoreInfoStatus of(boolean... filled) {
        for (boolean each : filled) {
            if (each) {
                return COMPLETED;
            }
        }
        return NOT_WRITTEN;
    }
}
