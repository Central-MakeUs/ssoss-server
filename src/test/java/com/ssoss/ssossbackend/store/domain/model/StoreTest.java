package com.ssoss.ssossbackend.store.domain.model;

import java.time.DayOfWeek;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ssoss.ssossbackend.store.domain.model.StoreInfoStatus.COMPLETED;
import static com.ssoss.ssossbackend.store.domain.model.StoreInfoStatus.IN_PROGRESS;
import static com.ssoss.ssossbackend.store.domain.model.StoreInfoStatus.NOT_WRITTEN;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Store")
class StoreTest {

    @Nested
    @DisplayName("writeBasicInfo")
    class WriteBasicInfo {

        @Test
        @DisplayName("기본 정보만 바뀌고 운영 정보와 콘텐츠 정보는 그대로 남는다")
        void keepsOtherGroups_whenBasicInfoWritten() {
            Store store = StoreStub.filled();

            store.writeBasicInfo("보니스베이커리", StoreType.BAKERY, "서울 중구 을지로 200", null);

            assertThat(store.getName()).isEqualTo("보니스베이커리");
            assertThat(store.getType()).isEqualTo(StoreType.BAKERY);
            assertThat(store.getAddress()).isEqualTo("서울 중구 을지로 200");
            assertThat(store.getIntroduction()).isNull();
            assertThat(store.businessDayValues()).containsExactly(DayOfWeek.MONDAY);
            assertThat(store.getOpenTime()).isEqualTo("09:00");
            assertThat(store.getCloseTime()).isEqualTo("22:00");
            assertThat(store.signatureMenuValues()).containsExactly("크루아상");
            assertThat(store.isTakeoutAvailable()).isTrue();
            assertThat(store.isReservationAvailable()).isFalse();
            assertThat(store.isParkingAvailable()).isTrue();
            assertThat(store.getStrength()).isEqualTo("직접 굽는 크루아상");
            assertThat(store.keywordValues()).containsExactly("디저트");
            assertThat(store.getForbidden()).isEqualTo("과장 표현");
            assertThat(store.getTone()).isEqualTo(Tone.CASUAL);
        }
    }

    @Nested
    @DisplayName("basicInfoStatus")
    class BasicInfoStatus {

        @Test
        @DisplayName("네 필드가 모두 비면 미작성이다")
        void notWritten_whenAllFieldsEmpty() {
            assertThat(StoreStub.basic(null, null, null, null).basicInfoStatus()).isEqualTo(NOT_WRITTEN);
        }

        @Test
        @DisplayName("필수 세 필드만 있고 한 줄 소개가 비면 작성 중이다")
        void inProgress_whenIntroductionMissing() {
            assertThat(StoreStub.basic("보니스커피", StoreType.CAFE, "서울 중구 을지로 100", null).basicInfoStatus())
                .isEqualTo(IN_PROGRESS);
        }

        @Test
        @DisplayName("한 줄 소개만 있어도 작성 중이다")
        void inProgress_whenOnlyIntroductionWritten() {
            assertThat(StoreStub.basic(null, null, null, "을지로 크루아상 카페").basicInfoStatus()).isEqualTo(IN_PROGRESS);
        }

        @Test
        @DisplayName("네 필드가 모두 있으면 작성 완료다")
        void completed_whenAllFieldsWritten() {
            assertThat(StoreStub.basic("보니스커피", StoreType.CAFE, "서울 중구 을지로 100", "을지로 크루아상 카페").basicInfoStatus())
                .isEqualTo(COMPLETED);
        }
    }

    @Nested
    @DisplayName("operationInfoStatus")
    class OperationInfoStatus {

        @Test
        @DisplayName("네 가지가 모두 비고 편의 시설이 셋 다 불가면 미작성이다")
        void notWritten_whenNothingWrittenAndNoAmenityAvailable() {
            assertThat(StoreStub.operation(null, null, null, null, false, false, false).operationInfoStatus())
                .isEqualTo(NOT_WRITTEN);
        }

        @Test
        @DisplayName("목록이 빈 배열이면 값이 없는 것으로 봐 미작성이다")
        void notWritten_whenListsAreEmpty() {
            assertThat(StoreStub.operation(new BusinessDays(List.of()), null, null, new SignatureMenus(List.of()),
                false, false, false).operationInfoStatus()).isEqualTo(NOT_WRITTEN);
        }

        @Test
        @DisplayName("영업 시각이 시작만 있으면 값이 없는 것으로 봐 미작성이다")
        void notWritten_whenOnlyOpenTimeWritten() {
            assertThat(StoreStub.operation(null, "09:00", null, null, false, false, false).operationInfoStatus())
                .isEqualTo(NOT_WRITTEN);
        }

        @Test
        @DisplayName("영업 시각은 시작과 종료가 둘 다 있어야 값이 있는 것으로 봐 작성 중이 된다")
        void inProgress_whenBothOpenAndCloseTimeWritten() {
            assertThat(StoreStub.operation(null, "09:00", "22:00", null, false, false, false).operationInfoStatus())
                .isEqualTo(IN_PROGRESS);
        }

        @Test
        @DisplayName("편의 시설 하나만 가능해도 값이 있는 것으로 봐 작성 중이 된다")
        void inProgress_whenSingleAmenityAvailable() {
            assertThat(StoreStub.operation(null, null, null, null, false, false, true).operationInfoStatus())
                .isEqualTo(IN_PROGRESS);
        }

        @Test
        @DisplayName("네 가지가 모두 있으면 작성 완료다")
        void completed_whenAllWritten() {
            assertThat(StoreStub.operation(new BusinessDays(List.of(DayOfWeek.MONDAY)), "09:00", "22:00",
                new SignatureMenus(List.of("크루아상")), true, false, false).operationInfoStatus())
                .isEqualTo(COMPLETED);
        }

        @Test
        @DisplayName("편의 시설이 셋 다 불가면 나머지를 다 채워도 작성 중에 머문다")
        void inProgress_whenEverythingButAmenitiesWritten() {
            assertThat(StoreStub.operation(new BusinessDays(List.of(DayOfWeek.MONDAY)), "09:00", "22:00",
                new SignatureMenus(List.of("크루아상")), false, false, false).operationInfoStatus())
                .isEqualTo(IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("contentInfoStatus")
    class ContentInfoStatus {

        @Test
        @DisplayName("네 필드가 모두 비면 미작성이다")
        void notWritten_whenAllFieldsEmpty() {
            assertThat(StoreStub.content(null, null, null, null).contentInfoStatus()).isEqualTo(NOT_WRITTEN);
        }

        @Test
        @DisplayName("키워드가 빈 배열이면 값이 없는 것으로 봐 미작성이다")
        void notWritten_whenKeywordsAreEmpty() {
            assertThat(StoreStub.content(null, new StoreKeywords(List.of()), null, null).contentInfoStatus())
                .isEqualTo(NOT_WRITTEN);
        }

        @Test
        @DisplayName("톤만 골라도 작성 중이다")
        void inProgress_whenOnlyToneWritten() {
            assertThat(StoreStub.content(null, null, null, Tone.CASUAL).contentInfoStatus()).isEqualTo(IN_PROGRESS);
        }

        @Test
        @DisplayName("네 필드가 모두 있으면 작성 완료다")
        void completed_whenAllFieldsWritten() {
            assertThat(StoreStub.content("직접 굽는 크루아상", new StoreKeywords(List.of("디저트")), "과장 표현", Tone.CASUAL)
                .contentInfoStatus()).isEqualTo(COMPLETED);
        }
    }
}
