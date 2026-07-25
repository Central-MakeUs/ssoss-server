package com.ssoss.ssossbackend.content.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChannelOutcome")
class ChannelOutcomeTest {

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("성공 상태는 성공 판정으로 접힌다")
        void foldsSucceeded_intoSucceeded() {
            assertThat(ChannelOutcome.from(GenerationResultStatus.SUCCEEDED))
                .isEqualTo(ChannelOutcome.SUCCEEDED);
        }

        @Test
        @DisplayName("429·5xx·커넥션 오류는 서버 과부하로 접힌다")
        void foldsTransportFailures_intoOverloaded() {
            assertThat(ChannelOutcome.from(GenerationResultStatus.RATE_LIMITED))
                .isEqualTo(ChannelOutcome.OVERLOADED);
            assertThat(ChannelOutcome.from(GenerationResultStatus.SERVER_ERROR))
                .isEqualTo(ChannelOutcome.OVERLOADED);
            assertThat(ChannelOutcome.from(GenerationResultStatus.CONNECTION_ERROR))
                .isEqualTo(ChannelOutcome.OVERLOADED);
        }

        @Test
        @DisplayName("타임아웃·지각 폐기는 시간 초과로 접힌다")
        void foldsTimeoutFailures_intoTimedOut() {
            assertThat(ChannelOutcome.from(GenerationResultStatus.TIMEOUT))
                .isEqualTo(ChannelOutcome.TIMED_OUT);
            assertThat(ChannelOutcome.from(GenerationResultStatus.DISCARDED_LATE))
                .isEqualTo(ChannelOutcome.TIMED_OUT);
        }

        @Test
        @DisplayName("빈 산출은 빈 결과로 접힌다")
        void foldsEmptyOutput_intoEmptyOutput() {
            assertThat(ChannelOutcome.from(GenerationResultStatus.EMPTY_OUTPUT))
                .isEqualTo(ChannelOutcome.EMPTY_OUTPUT);
        }
    }

    @Nested
    @DisplayName("status 와 message")
    class StatusAndMessage {

        @Test
        @DisplayName("판정마다 채널 상태와 사용자에게 보여줄 문구를 갖는다")
        void hasChannelStatusAndUserFacingMessage_perOutcome() {
            assertThat(ChannelOutcome.PENDING.getStatus()).isEqualTo(ChannelStatus.PENDING);
            assertThat(ChannelOutcome.PENDING.getMessage()).isEqualTo("생성 중입니다");

            assertThat(ChannelOutcome.SUCCEEDED.getStatus()).isEqualTo(ChannelStatus.SUCCEEDED);
            assertThat(ChannelOutcome.SUCCEEDED.getMessage()).isEqualTo("생성에 성공했습니다");

            assertThat(ChannelOutcome.OVERLOADED.getStatus()).isEqualTo(ChannelStatus.FAILED);
            assertThat(ChannelOutcome.OVERLOADED.getMessage())
                .isEqualTo("요청이 많습니다. 잠시 후 다시 시도해 주세요");

            assertThat(ChannelOutcome.TIMED_OUT.getStatus()).isEqualTo(ChannelStatus.FAILED);
            assertThat(ChannelOutcome.TIMED_OUT.getMessage())
                .isEqualTo("생성 시간이 초과되었습니다. 다시 시도해 주세요");

            assertThat(ChannelOutcome.EMPTY_OUTPUT.getStatus()).isEqualTo(ChannelStatus.FAILED);
            assertThat(ChannelOutcome.EMPTY_OUTPUT.getMessage())
                .isEqualTo("결과를 만들지 못했습니다. 다시 시도해 주세요");
        }
    }
}
