package com.ssoss.ssossbackend.store.application.service;

import java.time.DayOfWeek;
import java.util.List;

import com.ssoss.ssossbackend.store.domain.model.Amenities;
import com.ssoss.ssossbackend.store.domain.model.BusinessDays;
import com.ssoss.ssossbackend.store.domain.model.SignatureMenus;
import com.ssoss.ssossbackend.store.domain.model.Store;
import com.ssoss.ssossbackend.store.domain.model.StoreType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StoreProfile")
class StoreProfileTest {

    private static Store written() {
        Store store = Store.create(1L);
        store.writeBasicInfo("보니스커피", StoreType.BAKERY_CAFE, "서울 중구 을지로 100", "을지로 크루아상 카페");
        store.writeOperationInfo(new BusinessDays(List.of(DayOfWeek.MONDAY, DayOfWeek.SUNDAY)), "09:00", "22:00",
            new SignatureMenus(List.of("크루아상", "아메리카노")), new Amenities(true, false, true));
        return store;
    }

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("매장 유형을 한글 이름으로 낮춘다")
        void lowersType_toKoreanName() {
            assertThat(StoreProfile.from(written()).type()).isEqualTo("베이커리 카페");
        }

        @Test
        @DisplayName("영업 요일을 표기하지 않은 요일 값 그대로 담는다")
        void carriesBusinessDays_asRawValues() {
            assertThat(StoreProfile.from(written()).businessDays())
                .containsExactly(DayOfWeek.MONDAY, DayOfWeek.SUNDAY);
        }

        @Test
        @DisplayName("편의 시설은 가능한 것만 담는다")
        void keepsAvailableAmenitiesOnly() {
            assertThat(StoreProfile.from(written()).amenities()).containsExactly("포장 가능", "주차 가능");
        }

        @Test
        @DisplayName("기본 정보와 운영 정보의 나머지 값을 그대로 담는다")
        void carriesRemainingFields() {
            StoreProfile profile = StoreProfile.from(written());

            assertThat(profile.name()).isEqualTo("보니스커피");
            assertThat(profile.address()).isEqualTo("서울 중구 을지로 100");
            assertThat(profile.introduction()).isEqualTo("을지로 크루아상 카페");
            assertThat(profile.openTime()).isEqualTo("09:00");
            assertThat(profile.closeTime()).isEqualTo("22:00");
            assertThat(profile.signatureMenus()).containsExactly("크루아상", "아메리카노");
        }

        @Test
        @DisplayName("아무것도 입력하지 않은 매장은 값이 비고 목록이 빈다")
        void hasEmptyValues_whenNothingWritten() {
            StoreProfile profile = StoreProfile.from(Store.create(1L));

            assertThat(profile.name()).isNull();
            assertThat(profile.type()).isNull();
            assertThat(profile.address()).isNull();
            assertThat(profile.introduction()).isNull();
            assertThat(profile.openTime()).isNull();
            assertThat(profile.closeTime()).isNull();
            assertThat(profile.businessDays()).isEmpty();
            assertThat(profile.signatureMenus()).isEmpty();
            assertThat(profile.amenities()).isEmpty();
        }
    }
}
