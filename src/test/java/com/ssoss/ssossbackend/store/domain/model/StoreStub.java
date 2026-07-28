package com.ssoss.ssossbackend.store.domain.model;

import java.time.DayOfWeek;
import java.util.List;

class StoreStub {

    static Store filled() {
        return new Store(1L, 1L, "보니스커피", StoreType.CAFE, "서울 중구 을지로 100", "을지로 크루아상 카페",
            new BusinessDays(List.of(DayOfWeek.MONDAY)), "09:00", "22:00", new SignatureMenus(List.of("크루아상")),
            true, false, true, "직접 굽는 크루아상", new StoreKeywords(List.of("디저트")), "과장 표현", Tone.CASUAL);
    }

    static Store basic(String name, StoreType type, String address, String introduction) {
        return new Store(1L, 1L, name, type, address, introduction, null, null, null, null,
            false, false, false, null, null, null, null);
    }

    static Store operation(BusinessDays businessDays, String openTime, String closeTime,
        SignatureMenus signatureMenus, boolean takeout, boolean reservation, boolean parking) {
        return new Store(1L, 1L, null, null, null, null, businessDays, openTime, closeTime, signatureMenus,
            takeout, reservation, parking, null, null, null, null);
    }

    static Store content(String strength, StoreKeywords keywords, String forbidden, Tone tone) {
        return new Store(1L, 1L, null, null, null, null, null, null, null, null,
            false, false, false, strength, keywords, forbidden, tone);
    }
}
