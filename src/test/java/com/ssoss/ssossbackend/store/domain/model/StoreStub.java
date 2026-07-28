package com.ssoss.ssossbackend.store.domain.model;

class StoreStub {

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
