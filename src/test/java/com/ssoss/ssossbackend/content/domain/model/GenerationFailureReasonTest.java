package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenerationFailureReason")
class GenerationFailureReasonTest {

    private GenerationResult result(GenerationResultStatus status) {
        return new GenerationResult(null, 1L, Channel.BLOG, status, null, null, null, 100L, null, null, null);
    }

    @Nested
    @DisplayName("derive")
    class Derive {

        @Test
        @DisplayName("429·5xx·커넥션 오류는 서버 과부하로 접힌다")
        void foldsTransportFailures_intoOverloaded() {
            assertThat(GenerationFailureReason.derive(List.of(result(GenerationResultStatus.RATE_LIMITED))))
                .contains(GenerationFailureReason.OVERLOADED);
            assertThat(GenerationFailureReason.derive(List.of(result(GenerationResultStatus.SERVER_ERROR))))
                .contains(GenerationFailureReason.OVERLOADED);
            assertThat(GenerationFailureReason.derive(List.of(result(GenerationResultStatus.CONNECTION_ERROR))))
                .contains(GenerationFailureReason.OVERLOADED);
        }

        @Test
        @DisplayName("타임아웃·지각 폐기는 시간 초과로 접힌다")
        void foldsTimeoutFailures_intoTimedOut() {
            assertThat(GenerationFailureReason.derive(List.of(result(GenerationResultStatus.TIMEOUT))))
                .contains(GenerationFailureReason.TIMED_OUT);
            assertThat(GenerationFailureReason.derive(List.of(result(GenerationResultStatus.DISCARDED_LATE))))
                .contains(GenerationFailureReason.TIMED_OUT);
        }

        @Test
        @DisplayName("여러 사유가 섞이면 과부하가 우선한다")
        void prefersOverloaded_whenMixedFailures() {
            List<GenerationResult> results = List.of(
                result(GenerationResultStatus.EMPTY_OUTPUT), result(GenerationResultStatus.SERVER_ERROR));

            assertThat(GenerationFailureReason.derive(results)).contains(GenerationFailureReason.OVERLOADED);
        }

        @Test
        @DisplayName("성공 상태만 있으면 사유가 비어 있다")
        void returnsEmpty_whenOnlySucceededResults() {
            assertThat(GenerationFailureReason.derive(List.of(result(GenerationResultStatus.SUCCEEDED)))).isEmpty();
        }

        @Test
        @DisplayName("결과 행이 없으면 사유가 비어 있다")
        void returnsEmpty_whenNoResults() {
            assertThat(GenerationFailureReason.derive(List.of())).isEmpty();
        }
    }
}
