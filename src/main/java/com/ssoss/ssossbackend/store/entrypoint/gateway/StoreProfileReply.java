package com.ssoss.ssossbackend.store.entrypoint.gateway;

import java.time.DayOfWeek;
import java.util.List;

import com.ssoss.ssossbackend.store.application.result.StoreProfileResult;

public record StoreProfileReply(
    String name,
    String type,
    String address,
    String introduction,
    List<DayOfWeek> businessDays,
    String openTime,
    String closeTime,
    List<String> signatureMenus,
    List<String> amenities
) {

    static StoreProfileReply from(StoreProfileResult result) {
        return new StoreProfileReply(result.name(), result.type(), result.address(), result.introduction(),
            result.businessDays(), result.openTime(), result.closeTime(),
            result.signatureMenus(), result.amenities());
    }
}
