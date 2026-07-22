package com.ssoss.ssossbackend.credit.domain.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreditCycle")
class CreditCycleTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static Instant kst(int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, KST).toInstant();
    }

    @Nested
    @DisplayName("current")
    class Current {

        @Test
        @DisplayName("3일 이후에 조회하면 당월 3일 00:00 KST 가 사이클 시작이 된다")
        void startsAtThisMonthAnchor_whenQueriedAfterAnchorDay() {
            Instant now = kst(2026, 7, 15, 12, 30);

            CreditCycle cycle = CreditCycle.current(now);

            assertThat(cycle.startsAt()).isEqualTo(kst(2026, 7, 3, 0, 0));
        }

        @Test
        @DisplayName("3일 00:00 KST 정각에 조회하면 당월 3일이 사이클 시작이 된다")
        void startsAtThisMonthAnchor_whenQueriedExactlyAtAnchor() {
            Instant now = kst(2026, 7, 3, 0, 0);

            CreditCycle cycle = CreditCycle.current(now);

            assertThat(cycle.startsAt()).isEqualTo(kst(2026, 7, 3, 0, 0));
        }

        @Test
        @DisplayName("3일 이전에 조회하면 지난달 3일 00:00 KST 가 사이클 시작이 된다")
        void startsAtLastMonthAnchor_whenQueriedBeforeAnchorDay() {
            Instant now = kst(2026, 7, 2, 23, 59);

            CreditCycle cycle = CreditCycle.current(now);

            assertThat(cycle.startsAt()).isEqualTo(kst(2026, 6, 3, 0, 0));
        }

        @Test
        @DisplayName("1월 3일 이전에 조회하면 지난해 12월 3일이 사이클 시작이 된다")
        void startsAtLastYearDecemberAnchor_whenQueriedBeforeJanuaryAnchor() {
            Instant now = kst(2026, 1, 2, 12, 0);

            CreditCycle cycle = CreditCycle.current(now);

            assertThat(cycle.startsAt()).isEqualTo(kst(2025, 12, 3, 0, 0));
        }

        @Test
        @DisplayName("UTC 날짜로는 2일이라도 KST 로 3일이면 당월 3일이 사이클 시작이 된다")
        void derivesAnchorByKstDate_whenUtcDateIsStillOnPreviousDay() {
            Instant now = Instant.parse("2026-07-02T15:00:00Z");

            CreditCycle cycle = CreditCycle.current(now);

            assertThat(cycle.startsAt()).isEqualTo(kst(2026, 7, 3, 0, 0));
        }
    }
}
