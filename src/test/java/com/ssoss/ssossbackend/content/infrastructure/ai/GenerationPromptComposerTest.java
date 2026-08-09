package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.GenerationMaterial;
import com.ssoss.ssossbackend.content.domain.model.Purpose;
import com.ssoss.ssossbackend.content.domain.model.StoreMaterial;
import com.ssoss.ssossbackend.content.domain.model.StyleSource;
import com.ssoss.ssossbackend.content.domain.model.Tone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenerationPromptComposer")
class GenerationPromptComposerTest {

    private static final StoreMaterial NO_STORE = new StoreMaterial(null, null, null, null,
        List.of(), null, null, List.of(), List.of());

    private final GenerationPromptComposer composer = new GenerationPromptComposer(new StoreSectionComposer(),
        new StyleSourceSectionComposer());

    private static GenerationMaterial materialWithStore(StoreMaterial store) {
        return new GenerationMaterial(Channel.BLOG, Purpose.INFORMATION, Tone.CASUAL, "주말 이벤트", null, List.of(),
            false, store, StyleSource.none());
    }

    private static GenerationMaterial materialWithStyleSource(StyleSource styleSource) {
        return new GenerationMaterial(Channel.BLOG, Purpose.INFORMATION, Tone.CASUAL, "주말 이벤트", null, List.of(),
            false, NO_STORE, styleSource);
    }

    @Nested
    @DisplayName("매장 정보 절")
    class StoreSection {

        @Test
        @DisplayName("매장 정보를 다 채우면 값이 모두 실린다")
        void carriesEveryField_whenStoreFullyWritten() {
            String prompt = composer.compose(materialWithStore(new StoreMaterial("보니스커피", "카페", "서울 중구 을지로 100",
                "을지로 크루아상 카페", List.of("월요일", "화요일"), "09:00", "22:00",
                List.of("크루아상", "아메리카노"), List.of("포장 가능", "주차 가능"))));

            assertThat(prompt).contains("""
                [매장 정보]
                아래는 이 매장에 대한 사실이다. 여기 없는 사실은 지어내지 않는다.
                매장명: 보니스커피
                매장 유형: 카페
                주소: 서울 중구 을지로 100
                한 줄 소개: 을지로 크루아상 카페
                영업일: 월요일, 화요일
                영업 시간: 09:00~22:00
                대표 메뉴: 크루아상, 아메리카노
                편의 시설: 포장 가능, 주차 가능""");
        }

        @Test
        @DisplayName("운영 정보를 안 채우면 그 줄이 통째로 빠진다")
        void omitsOperationLines_whenOperationInfoMissing() {
            String prompt = composer.compose(materialWithStore(new StoreMaterial("보니스커피", "카페", "서울 중구 을지로 100",
                "을지로 크루아상 카페", List.of(), null, null, List.of(), List.of())));

            assertThat(prompt)
                .contains("매장명: 보니스커피")
                .doesNotContain("영업일:", "영업 시간:", "대표 메뉴:", "편의 시설:");
        }

        @Test
        @DisplayName("영업 시각은 시작과 종료가 둘 다 있어야 실린다")
        void omitsBusinessHours_whenOnlyOpenTimeWritten() {
            String prompt = composer.compose(materialWithStore(new StoreMaterial("보니스커피", null, null, null,
                List.of(), "09:00", null, List.of(), List.of())));

            assertThat(prompt).doesNotContain("영업 시간:");
        }

        @Test
        @DisplayName("빈 항목만 줄에서 빠지고 채운 항목은 남는다")
        void keepsWrittenLinesOnly_whenPartiallyWritten() {
            String prompt = composer.compose(materialWithStore(new StoreMaterial("보니스커피", null, null, null,
                List.of(), null, null, List.of("크루아상"), List.of())));

            assertThat(prompt)
                .contains("매장명: 보니스커피")
                .contains("대표 메뉴: 크루아상")
                .doesNotContain("매장 유형:", "주소:", "한 줄 소개:");
        }

        @Test
        @DisplayName("기본 정보를 건너뛰고 운영 정보만 채워도 그 값이 실린다")
        void carriesOperationLines_whenOnlyOperationInfoWritten() {
            String prompt = composer.compose(materialWithStore(new StoreMaterial(null, null, null, null,
                List.of("월요일", "화요일"), "09:00", "22:00", List.of("크루아상"), List.of("포장 가능"))));

            assertThat(prompt)
                .contains("영업일: 월요일, 화요일")
                .contains("영업 시간: 09:00~22:00")
                .contains("대표 메뉴: 크루아상")
                .contains("편의 시설: 포장 가능")
                .doesNotContain("매장명:", "매장 정보가 제공되지 않았다");
        }

        @Test
        @DisplayName("매장명이 공백뿐이면 매장명 줄만 빠진다")
        void omitsNameLine_whenNameBlank() {
            String prompt = composer.compose(materialWithStore(new StoreMaterial("   ", "카페", null, null,
                List.of(), null, null, List.of(), List.of())));

            assertThat(prompt)
                .contains("매장 유형: 카페")
                .doesNotContain("매장명:", "매장 정보가 제공되지 않았다");
        }

        @Test
        @DisplayName("모든 항목이 비면 매장 정보가 없다는 문구가 실린다")
        void carriesNoStoreNotice_whenNothingWritten() {
            String prompt = composer.compose(materialWithStore(new StoreMaterial(null, null, null, null,
                List.of(), null, null, List.of(), List.of())));

            assertThat(prompt)
                .contains("매장 정보가 제공되지 않았다")
                .doesNotContain("매장명:");
        }

        @Test
        @DisplayName("영업 시작 시각만 있으면 실을 값이 없어 매장 정보가 없는 것으로 본다")
        void carriesNoStoreNotice_whenOnlyOpenTimeWrittenAndNothingElse() {
            String prompt = composer.compose(materialWithStore(new StoreMaterial(null, null, null, null,
                List.of(), "09:00", null, List.of(), List.of())));

            assertThat(prompt).contains("매장 정보가 제공되지 않았다");
        }
    }

    @Nested
    @DisplayName("참고 글 절")
    class StyleSourceSection {

        @Test
        @DisplayName("참고 글이 없으면 참고 글 절과 참고 범위 절이 통째로 빠진다")
        void omitsSections_whenNoStyleSource() {
            String prompt = composer.compose(materialWithStore(NO_STORE));

            assertThat(prompt).doesNotContain("[참고 글]", "[참고 범위]");
        }

        @Test
        @DisplayName("참고 글이 있으면 제목·본문과 참고 범위 지시가 함께 실린다")
        void carriesTitleBodyAndScope_whenStyleSourceGiven() {
            String prompt = composer.compose(materialWithStyleSource(
                new StyleSource("을지로 크루아상 맛집", "겹겹이 살아있는 결을 만나 보세요.")));

            assertThat(prompt)
                .contains("[참고 글]")
                .contains("제목: 을지로 크루아상 맛집")
                .contains("겹겹이 살아있는 결을 만나 보세요.")
                .contains("[참고 범위]")
                .contains("말투·문장 구성·분량뿐이다");
        }

        @Test
        @DisplayName("제목 없는 채널의 참고 글이면 제목 줄만 빠진다")
        void omitsTitleLine_whenStyleSourceHasNoTitle() {
            String prompt = composer.compose(materialWithStyleSource(new StyleSource(null, "겹겹이 살아있는 결을 만나 보세요.")));

            assertThat(prompt)
                .contains("[참고 글]")
                .contains("겹겹이 살아있는 결을 만나 보세요.")
                .doesNotContain("제목:");
        }
    }
}
