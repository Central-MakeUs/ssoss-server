package com.ssoss.ssossbackend.credit.domain.model;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Credit")
class CreditTest {

    private static final CreditCycle CYCLE = CreditCycle.current(Instant.parse("2026-07-15T00:00:00Z"));
    private static final CreditCycle NEXT_CYCLE = CreditCycle.current(Instant.parse("2026-08-15T00:00:00Z"));

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("잔액 행을 만들면 무료·충전 잔액이 모두 0 으로 시작한다")
        void startsWithZeroBalances_whenCreated() {
            Credit credit = Credit.create(1L);

            assertThat(credit.getFreeBalance()).isZero();
            assertThat(credit.getChargedBalance()).isZero();
            assertThat(credit.balance()).isZero();
        }
    }

    @Nested
    @DisplayName("grant")
    class Grant {

        @Test
        @DisplayName("무료 크레딧을 지급하면 무료 잔액에 더해지고 지급 사이클이 기록된다")
        void addsToFreeBalanceAndRecordsCycle_whenFreeCreditGranted() {
            Credit credit = Credit.create(1L).grant(Credit.CYCLE_FREE_GRANT, CYCLE);

            assertThat(credit.getFreeBalance()).isEqualTo(50);
            assertThat(credit.getChargedBalance()).isZero();
            assertThat(credit.getGrantedCycleAt()).isEqualTo(CYCLE.startsAt());
        }
    }

    @Nested
    @DisplayName("isGrantedFor")
    class IsGrantedFor {

        @Test
        @DisplayName("지급받은 사이클로 판정하면 지급된 것으로 본다")
        void returnsTrue_whenQueriedWithGrantedCycle() {
            Credit credit = Credit.create(1L).grant(Credit.CYCLE_FREE_GRANT, CYCLE);

            assertThat(credit.isGrantedFor(CYCLE)).isTrue();
        }

        @Test
        @DisplayName("지급받은 적 없는 사이클로 판정하면 지급되지 않은 것으로 본다")
        void returnsFalse_whenQueriedWithDifferentCycle() {
            Credit credit = Credit.create(1L).grant(Credit.CYCLE_FREE_GRANT, CYCLE);

            assertThat(credit.isGrantedFor(NEXT_CYCLE)).isFalse();
        }

        @Test
        @DisplayName("지급 이력이 없으면 지급되지 않은 것으로 본다")
        void returnsFalse_whenNeverGranted() {
            Credit credit = Credit.create(1L);

            assertThat(credit.isGrantedFor(CYCLE)).isFalse();
        }
    }

    @Nested
    @DisplayName("expireFree")
    class ExpireFree {

        @Test
        @DisplayName("무료 잔액을 소멸하면 무료 잔액이 0 이 되고 소멸량이 반환된다")
        void zeroesFreeBalanceAndReturnsExpiredAmount_whenFreeBalanceExpired() {
            Credit credit = Credit.create(1L).grant(30, CYCLE);

            int expired = credit.expireFree();

            assertThat(expired).isEqualTo(30);
            assertThat(credit.getFreeBalance()).isZero();
        }

        @Test
        @DisplayName("무료 잔액이 없으면 소멸량은 0 이다")
        void returnsZero_whenFreeBalanceIsEmpty() {
            Credit credit = Credit.create(1L);

            int expired = credit.expireFree();

            assertThat(expired).isZero();
            assertThat(credit.getFreeBalance()).isZero();
        }
    }

    @Nested
    @DisplayName("balance")
    class Balance {

        @Test
        @DisplayName("잔액은 무료 잔액과 충전 잔액의 합이다")
        void sumsFreeAndChargedBalances_whenQueried() {
            Credit credit = Credit.create(1L).grant(30, CYCLE);

            assertThat(credit.balance()).isEqualTo(30);
        }
    }
}
