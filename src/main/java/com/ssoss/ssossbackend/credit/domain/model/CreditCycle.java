package com.ssoss.ssossbackend.credit.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public record CreditCycle(Instant startsAt) {

    private static final int ANCHOR_DAY_OF_MONTH = 3;
    private static final ZoneId ANCHOR_ZONE = ZoneId.of("Asia/Seoul");

    public static CreditCycle current(Instant now) {
        LocalDate today = now.atZone(ANCHOR_ZONE).toLocalDate();
        LocalDate anchor = today.withDayOfMonth(ANCHOR_DAY_OF_MONTH);
        if (today.isBefore(anchor)) {
            anchor = anchor.minusMonths(1);
        }
        return new CreditCycle(anchor.atStartOfDay(ANCHOR_ZONE).toInstant());
    }
}
