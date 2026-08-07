package com.ssoss.ssossbackend.template.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

public record StoreOperatingHours(List<DayOfWeek> days, String openTime, String closeTime) {

    private static final DateTimeFormatter MERIDIEM_TIME = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN);
    private static final String DAY_DELIMITER = ", ";

    public StoreOperatingHours {
        days = days == null ? List.of() : List.copyOf(days);
    }

    public String format() {
        if (days.isEmpty() || !StringUtils.hasText(openTime) || !StringUtils.hasText(closeTime)) {
            return null;
        }
        return "%s %s ~ %s".formatted(
            days.stream()
                .map(day -> day.getDisplayName(TextStyle.SHORT, Locale.KOREAN))
                .collect(Collectors.joining(DAY_DELIMITER)),
            MERIDIEM_TIME.format(LocalTime.parse(openTime)),
            MERIDIEM_TIME.format(LocalTime.parse(closeTime)));
    }
}
