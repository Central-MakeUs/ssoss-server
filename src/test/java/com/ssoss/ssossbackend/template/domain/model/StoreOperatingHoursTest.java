package com.ssoss.ssossbackend.template.domain.model;

import java.time.DayOfWeek;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StoreOperatingHours")
class StoreOperatingHoursTest {

    private static final List<DayOfWeek> WEDNESDAY_TO_SUNDAY = List.of(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    @Nested
    @DisplayName("format")
    class Format {

        @Test
        @DisplayName("영업 요일과 시각을 요일 약자와 오전·오후 표기로 조립한다")
        void assemblesDaysAndClock_whenBothGiven() {
            StoreOperatingHours hours = new StoreOperatingHours(WEDNESDAY_TO_SUNDAY, "09:00", "20:00");

            assertThat(hours.format()).isEqualTo("수, 목, 금, 토, 일 오전 9:00 ~ 오후 8:00");
        }

        @Test
        @DisplayName("영업 요일이 하루면 그 하루만 적는다")
        void writesSingleDay_whenOnlyOneDayGiven() {
            StoreOperatingHours hours = new StoreOperatingHours(List.of(DayOfWeek.MONDAY), "10:30", "19:05");

            assertThat(hours.format()).isEqualTo("월 오전 10:30 ~ 오후 7:05");
        }

        @Test
        @DisplayName("정오와 자정을 12시로 적는다")
        void writesNoonAndMidnightAsTwelve() {
            StoreOperatingHours hours = new StoreOperatingHours(List.of(DayOfWeek.MONDAY), "00:00", "12:00");

            assertThat(hours.format()).isEqualTo("월 오전 12:00 ~ 오후 12:00");
        }

        @Test
        @DisplayName("영업 요일이 비면 조립하지 않는다")
        void skips_whenDaysMissing() {
            StoreOperatingHours hours = new StoreOperatingHours(List.of(), "09:00", "20:00");

            assertThat(hours.format()).isNull();
        }

        @Test
        @DisplayName("영업 시각이 없으면 조립하지 않는다")
        void skips_whenClockMissing() {
            StoreOperatingHours hours = new StoreOperatingHours(WEDNESDAY_TO_SUNDAY, null, null);

            assertThat(hours.format()).isNull();
        }

        @Test
        @DisplayName("여는 시각만 있으면 조립하지 않는다")
        void skips_whenCloseTimeMissing() {
            StoreOperatingHours hours = new StoreOperatingHours(WEDNESDAY_TO_SUNDAY, "09:00", null);

            assertThat(hours.format()).isNull();
        }
    }
}
