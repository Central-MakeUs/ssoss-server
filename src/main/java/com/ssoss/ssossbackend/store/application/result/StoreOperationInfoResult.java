package com.ssoss.ssossbackend.store.application.result;

import java.time.DayOfWeek;
import java.util.List;

import com.ssoss.ssossbackend.store.domain.model.Amenities;
import com.ssoss.ssossbackend.store.domain.model.Store;

public record StoreOperationInfoResult(
    List<String> businessDays,
    String openTime,
    String closeTime,
    List<String> signatureMenus,
    boolean takeoutAvailable,
    boolean reservationAvailable,
    boolean parkingAvailable,
    String status
) {

    public static StoreOperationInfoResult from(Store store) {
        Amenities amenities = store.getAmenities();
        return new StoreOperationInfoResult(
            store.businessDayValues().stream().map(DayOfWeek::name).toList(),
            store.getOpenTime(),
            store.getCloseTime(),
            store.signatureMenuValues(),
            amenities.takeoutAvailable(),
            amenities.reservationAvailable(),
            amenities.parkingAvailable(),
            store.operationInfoStatus().name());
    }
}
