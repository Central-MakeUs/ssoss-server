package com.ssoss.ssossbackend.content.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Generation")
class GenerationTest {

    private static final Instant CREATED_AT = Instant.parse("2026-07-22T10:00:00Z");
    private static final Instant DEADLINE = CREATED_AT.plus(Generation.DEADLINE);

    private Generation generation(List<Channel> channels) {
        Generation generation = Generation.create(1L, channels, Purpose.INFORMATION, Tone.CASUAL,
            "강조", null, null);
        return new Generation(1L, 1L, generation.getChannels(), generation.getPurpose(), generation.getTone(),
            generation.getEmphasis(), generation.getForbidden(), generation.getKeywords(),
            generation.isPhotoGuideChecked(), generation.getSourceSavedContentId(), CREATED_AT, null);
    }

    @Nested
    @DisplayName("finish")
    class Finish {

        @Test
        @DisplayName("deadline 이내에 끝나면 finished_at 이 기록되고 성공으로 파생된다")
        void recordsFinishedAtAndDerivesSucceeded_whenFinishedWithinDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            boolean finished = generation.finish(CREATED_AT.plusSeconds(10));

            assertThat(finished).isTrue();
            assertThat(generation.getFinishedAt()).isEqualTo(CREATED_AT.plusSeconds(10));
            assertThat(generation.status(CREATED_AT.plusSeconds(10), List.of(Channel.BLOG)))
                .isEqualTo(GenerationStatus.SUCCEEDED);
        }

        @Test
        @DisplayName("deadline 정각에 끝나면 이내로 인정된다")
        void recordsFinishedAt_whenFinishedExactlyAtDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            boolean finished = generation.finish(DEADLINE);

            assertThat(finished).isTrue();
            assertThat(generation.status(DEADLINE, List.of(Channel.BLOG))).isEqualTo(GenerationStatus.SUCCEEDED);
        }

        @Test
        @DisplayName("deadline 을 넘겨 끝나면 기록되지 않는다")
        void discardsFinish_whenFinishedAfterDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            boolean finished = generation.finish(DEADLINE.plusSeconds(1));

            assertThat(finished).isFalse();
            assertThat(generation.getFinishedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("status")
    class Status {

        @Test
        @DisplayName("finished_at 이 없고 deadline 이내면 진행 중으로 파생된다")
        void derivesInProgress_whenNotFinishedWithinDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.status(CREATED_AT.plusSeconds(10), List.of()))
                .isEqualTo(GenerationStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("finished_at 없이 deadline 이 지나면 실패로 파생된다")
        void derivesFailed_whenDeadlinePassedWithoutFinish() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.status(DEADLINE.plusSeconds(1), List.of()))
                .isEqualTo(GenerationStatus.FAILED);
        }

        @Test
        @DisplayName("끝났는데 성공한 채널이 없으면 실패로 파생된다")
        void derivesFailed_whenFinishedWithoutAnySuccess() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM));
            generation.finish(CREATED_AT.plusSeconds(10));

            assertThat(generation.status(CREATED_AT.plusSeconds(10), List.of()))
                .isEqualTo(GenerationStatus.FAILED);
        }

        @Test
        @DisplayName("끝났고 성공한 채널이 하나라도 있으면 성공으로 파생된다")
        void derivesSucceeded_whenFinishedWithAnySuccess() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM));
            generation.finish(CREATED_AT.plusSeconds(10));

            assertThat(generation.status(CREATED_AT.plusSeconds(10), List.of(Channel.BLOG)))
                .isEqualTo(GenerationStatus.SUCCEEDED);
        }
    }

    @Nested
    @DisplayName("deadlineBudget")
    class DeadlineBudget {

        @Test
        @DisplayName("deadline 까지 남은 시간이 예산으로 파생된다")
        void derivesRemainingTimeUntilDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.deadlineBudget(CREATED_AT.plusSeconds(20))).isEqualTo(Duration.ofSeconds(40));
        }

        @Test
        @DisplayName("deadline 이 지나면 예산은 0 이다")
        void returnsZero_whenPastDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.deadlineBudget(DEADLINE.plusSeconds(5))).isZero();
        }
    }

    @Nested
    @DisplayName("isExpired")
    class IsExpired {

        @Test
        @DisplayName("deadline 이내면 만료되지 않은 것으로 본다")
        void returnsFalse_whenWithinDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.isExpired(DEADLINE)).isFalse();
        }

        @Test
        @DisplayName("deadline 을 넘기면 만료된 것으로 본다")
        void returnsTrue_whenPastDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.isExpired(DEADLINE.plusSeconds(1))).isTrue();
        }
    }

    @Nested
    @DisplayName("channelList")
    class ChannelList {

        @Test
        @DisplayName("선택한 채널 목록이 순서대로 복원된다")
        void restoresSelectedChannelsInOrder() {
            Generation generation = generation(List.of(Channel.INSTAGRAM, Channel.BLOG, Channel.THREADS));

            assertThat(generation.channelList())
                .containsExactly(Channel.INSTAGRAM, Channel.BLOG, Channel.THREADS);
        }
    }

    @Nested
    @DisplayName("pendingChannels")
    class PendingChannels {

        @Test
        @DisplayName("진행 중 채널은 선택 채널에서 완료된 채널을 뺀 나머지다")
        void derivesPendingChannels_bySubtractingSettledChannels() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM, Channel.THREADS));

            List<Channel> pending = generation.pendingChannels(List.of(Channel.INSTAGRAM));

            assertThat(pending).containsExactly(Channel.BLOG, Channel.THREADS);
        }

        @Test
        @DisplayName("모든 채널이 완료되면 진행 중 채널이 비어 있다")
        void returnsEmpty_whenAllChannelsSettled() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM));

            List<Channel> pending = generation.pendingChannels(List.of(Channel.BLOG, Channel.INSTAGRAM));

            assertThat(pending).isEmpty();
        }
    }
}
