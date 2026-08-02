package com.ssoss.ssossbackend.store.application.service;

import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ssoss.ssossbackend.store.domain.model.Amenities;
import com.ssoss.ssossbackend.store.domain.model.Store;
import com.ssoss.ssossbackend.store.domain.model.StoreType;

public record StoreProfile(
    String name,
    String type,
    String address,
    String introduction,
    List<String> businessDays,
    String openTime,
    String closeTime,
    List<String> signatureMenus,
    List<String> amenities
) {

    private static final String TAKEOUT_AVAILABLE = "포장 가능";
    private static final String RESERVATION_AVAILABLE = "예약 가능";
    private static final String PARKING_AVAILABLE = "주차 가능";

    static StoreProfile from(Store store) {
        Amenities amenities = store.getAmenities();
        List<String> availableAmenities = new ArrayList<>();
        if (amenities.takeoutAvailable()) {
            availableAmenities.add(TAKEOUT_AVAILABLE);
        }
        if (amenities.reservationAvailable()) {
            availableAmenities.add(RESERVATION_AVAILABLE);
        }
        if (amenities.parkingAvailable()) {
            availableAmenities.add(PARKING_AVAILABLE);
        }
        StoreType type = store.getType();
        return new StoreProfile(
            store.getName(),
            type == null ? null : type.koreanName(),
            store.getAddress(),
            store.getIntroduction(),
            store.businessDayValues().stream()
                .map(day -> day.getDisplayName(TextStyle.FULL, Locale.KOREAN))
                .toList(),
            store.getOpenTime(),
            store.getCloseTime(),
            store.signatureMenuValues(),
            List.copyOf(availableAmenities));
    }
}
