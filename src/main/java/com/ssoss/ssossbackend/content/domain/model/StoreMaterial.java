package com.ssoss.ssossbackend.content.domain.model;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.util.StringUtils;

public record StoreMaterial(
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

    public StoreMaterial {
        businessDays = businessDays == null ? List.of() : List.copyOf(businessDays);
        signatureMenus = signatureMenus == null ? List.of() : List.copyOf(signatureMenus);
        amenities = amenities == null ? List.of() : List.copyOf(amenities);
    }

    public static StoreMaterial of(String name, String type, String address, String introduction,
        List<DayOfWeek> businessDays, String openTime, String closeTime,
        List<String> signatureMenus, List<String> amenities) {
        return new StoreMaterial(name, type, address, introduction,
            businessDays == null ? List.of()
                : businessDays.stream().map(day -> day.getDisplayName(TextStyle.FULL, Locale.KOREAN)).toList(),
            openTime, closeTime, signatureMenus, amenities);
    }

    public boolean isEmpty() {
        return !StringUtils.hasText(name)
            && !StringUtils.hasText(type)
            && !StringUtils.hasText(address)
            && !StringUtils.hasText(introduction)
            && businessDays.isEmpty()
            && !hasBusinessHours()
            && signatureMenus.isEmpty()
            && amenities.isEmpty();
    }

    public boolean hasBusinessHours() {
        return StringUtils.hasText(openTime) && StringUtils.hasText(closeTime);
    }
}
