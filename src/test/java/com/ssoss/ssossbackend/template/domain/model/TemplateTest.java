package com.ssoss.ssossbackend.template.domain.model;

import java.time.DayOfWeek;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Template")
class TemplateTest {

    private static final StoreOperatingHours NO_OPERATING_HOURS = new StoreOperatingHours(List.of(), null, null);
    private static final StoreInfo BONIS_COFFEE = new StoreInfo("보니스커피", "서울 중구 을지로 100",
        new StoreOperatingHours(List.of(DayOfWeek.MONDAY), "09:00", "20:00"));

    private static Template createTemplate(String body) {
        return new Template(1L, TemplateCategory.NEW_MENU, "신메뉴 출시 안내", "새로 나온 메뉴를 소개하는 글", body,
            "예시 본문", new RecommendedChannels(List.of(Channel.INSTAGRAM)));
    }

    @Nested
    @DisplayName("replacePlaceholders")
    class ReplacePlaceholders {

        @Test
        @DisplayName("아는 자리표시자를 매장 값으로 바꾼다")
        void fillsKnownPlaceholders_withStoreValues() {
            Template template = createTemplate("[가게명]입니다. 📍[주소] 🕐[영업시간]");

            assertThat(template.replacePlaceholders(BONIS_COFFEE))
                .isEqualTo("보니스커피입니다. 📍서울 중구 을지로 100 🕐월 오전 9:00 ~ 오후 8:00");
        }

        @Test
        @DisplayName("모르는 자리표시자는 건드리지 않는다")
        void keepsUnknownPlaceholders() {
            Template template = createTemplate("🎁신메뉴: [메뉴명] 📞[전화번호]");

            assertThat(template.replacePlaceholders(BONIS_COFFEE)).isEqualTo("🎁신메뉴: [메뉴명] 📞[전화번호]");
        }

        @Test
        @DisplayName("매장 값이 비면 그 자리표시자를 남긴다")
        void keepsPlaceholder_whenStoreValueBlank() {
            Template template = createTemplate("[가게명] 📍[주소]");
            StoreInfo nameless = new StoreInfo(null, "서울 중구 을지로 100", NO_OPERATING_HOURS);

            assertThat(template.replacePlaceholders(nameless)).isEqualTo("[가게명] 📍서울 중구 을지로 100");
        }

        @Test
        @DisplayName("채워 넣은 값에 다른 자리표시자가 섞여 있어도 다시 치환하지 않는다")
        void doesNotRefill_whenFilledValueContainsAnotherPlaceholder() {
            Template template = createTemplate("[가게명] 📍[주소]");
            StoreInfo trickyName = new StoreInfo("[주소]", "서울 중구 을지로 100", NO_OPERATING_HOURS);

            assertThat(template.replacePlaceholders(trickyName)).isEqualTo("[주소] 📍서울 중구 을지로 100");
        }

        @Test
        @DisplayName("매장 값에 정규식 치환 기호가 있어도 글자 그대로 넣는다")
        void insertsValueLiterally_whenValueHasReplacementSyntax() {
            Template template = createTemplate("[가게명] 어서 오세요");
            StoreInfo dollarName = new StoreInfo("$1 커피\\집", null, NO_OPERATING_HOURS);

            assertThat(template.replacePlaceholders(dollarName)).isEqualTo("$1 커피\\집 어서 오세요");
        }
    }
}
