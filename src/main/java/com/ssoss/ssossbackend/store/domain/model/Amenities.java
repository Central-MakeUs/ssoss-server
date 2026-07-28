package com.ssoss.ssossbackend.store.domain.model;

public record Amenities(boolean takeoutAvailable, boolean reservationAvailable, boolean parkingAvailable) {

    public boolean anyAvailable() {
        return takeoutAvailable || reservationAvailable || parkingAvailable;
    }
}
