package com.ssoss.ssossbackend.template.domain.model;

import java.time.DayOfWeek;
import java.util.List;

public record StoreInfo(String name, String address, StoreOperatingHours operatingHours) {

    public static StoreInfo of(String name, String address, List<DayOfWeek> businessDays,
        String openTime, String closeTime) {
        return new StoreInfo(name, address, new StoreOperatingHours(businessDays, openTime, closeTime));
    }
}
