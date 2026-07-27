package com.ssoss.ssossbackend.app.domain.model;

import com.ssoss.ssossbackend.shared.exception.BusinessException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("앱 버전")
class SemanticVersionTest {

    @Nested
    @DisplayName("버전 비교")
    class Comparison {

        @Test
        @DisplayName("마이너 버전이 두 자리여도 한 자리보다 높게 비교된다")
        void comparesTwoDigitMinorAsHigher() {
            assertThat(SemanticVersion.from("1.10.0").isLowerThan(SemanticVersion.from("1.9.0"))).isFalse();
            assertThat(SemanticVersion.from("1.9.0").isLowerThan(SemanticVersion.from("1.10.0"))).isTrue();
        }

        @Test
        @DisplayName("같은 버전은 서로 낮지 않다")
        void treatsEqualVersionsAsNotLower() {
            assertThat(SemanticVersion.from("1.2.3").isLowerThan(SemanticVersion.from("1.2.3"))).isFalse();
        }

        @Test
        @DisplayName("메이저 버전이 낮으면 마이너와 패치가 높아도 낮다")
        void comparesMajorFirst() {
            assertThat(SemanticVersion.from("1.99.99").isLowerThan(SemanticVersion.from("2.0.0"))).isTrue();
        }

        @Test
        @DisplayName("메이저와 마이너가 같으면 패치로 비교한다")
        void comparesPatchLast() {
            assertThat(SemanticVersion.from("1.2.3").isLowerThan(SemanticVersion.from("1.2.4"))).isTrue();
            assertThat(SemanticVersion.from("1.2.4").isLowerThan(SemanticVersion.from("1.2.3"))).isFalse();
        }
    }

    @Nested
    @DisplayName("형식 검사")
    class Format {

        @Test
        @DisplayName("세 자리를 점으로 이은 형식을 읽는다")
        void parsesThreeParts() {
            assertThat(SemanticVersion.from("1.10.234")).isEqualTo(new SemanticVersion(1, 10, 234));
        }

        @Test
        @DisplayName("패치가 없으면 AP0003 을 던진다")
        void throwsWhenPatchMissing() {
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> SemanticVersion.from("1.0"))
                .extracting(BusinessException::getErrorCode)
                .isEqualTo(AppErrorCode.INVALID_APP_VERSION);
        }

        @Test
        @DisplayName("숫자가 아닌 값이 섞이면 AP0003 을 던진다")
        void throwsWhenNotNumeric() {
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> SemanticVersion.from("1.0.0-beta"))
                .extracting(BusinessException::getErrorCode)
                .isEqualTo(AppErrorCode.INVALID_APP_VERSION);
        }

        @Test
        @DisplayName("값이 없거나 비어 있으면 AP0003 을 던진다")
        void throwsWhenMissingOrBlank() {
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> SemanticVersion.from(null))
                .extracting(BusinessException::getErrorCode)
                .isEqualTo(AppErrorCode.INVALID_APP_VERSION);
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> SemanticVersion.from(""))
                .extracting(BusinessException::getErrorCode)
                .isEqualTo(AppErrorCode.INVALID_APP_VERSION);
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> SemanticVersion.from("   "))
                .extracting(BusinessException::getErrorCode)
                .isEqualTo(AppErrorCode.INVALID_APP_VERSION);
        }
    }
}
