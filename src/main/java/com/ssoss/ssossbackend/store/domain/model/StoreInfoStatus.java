package com.ssoss.ssossbackend.store.domain.model;

public enum StoreInfoStatus {

    NOT_WRITTEN,
    IN_PROGRESS,
    COMPLETED;

    static StoreInfoStatus of(boolean... filled) {
        int written = 0;
        for (boolean each : filled) {
            if (each) {
                written++;
            }
        }
        if (written == 0) {
            return NOT_WRITTEN;
        }
        return written == filled.length ? COMPLETED : IN_PROGRESS;
    }
}
