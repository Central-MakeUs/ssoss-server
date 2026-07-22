package com.ssoss.ssossbackend.credit.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Credit")
class CreditTest {

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
        @DisplayName("무료 크레딧을 지급하면 무료 잔액에 더해진다")
        void addsToFreeBalance_whenFreeCreditGranted() {
            Credit credit = Credit.create(1L).grant(Credit.CYCLE_FREE_GRANT);

            assertThat(credit.getFreeBalance()).isEqualTo(50);
            assertThat(credit.getChargedBalance()).isZero();
        }
    }

    @Nested
    @DisplayName("balance")
    class Balance {

        @Test
        @DisplayName("잔액은 무료 잔액과 충전 잔액의 합이다")
        void sumsFreeAndChargedBalances_whenQueried() {
            Credit credit = Credit.create(1L).grant(30);

            assertThat(credit.balance()).isEqualTo(30);
        }
    }
}
