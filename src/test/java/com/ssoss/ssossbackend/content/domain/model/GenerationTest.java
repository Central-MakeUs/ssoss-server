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

    private GenerationResult succeeded(Channel channel) {
        return GenerationResult.succeeded(1L, channel,
            new LlmCallReply(new GeneratedContent("제목", "본문", List.of("#태그")), 100L, 10, 20, "{}"));
    }

    private GenerationResult failed(Channel channel) {
        return GenerationResult.failed(1L, channel, GenerationResultStatus.RATE_LIMITED, 100L, null, null, null);
    }

    private Generation generation(List<Channel> channels) {
        Generation generation = Generation.create(1L, channels, Purpose.INFORMATION, Tone.CASUAL,
            "강조", null, null, false);
        return new Generation(1L, 1L, generation.getChannels(), generation.getPurpose(), generation.getTone(),
            generation.getEmphasis(), generation.getForbidden(), generation.getKeywords(),
            generation.isPhotoGuideChecked(), generation.getSourceContentChannelId(), CREATED_AT, null);
    }

    @Nested
    @DisplayName("finish")
    class Finish {

        @Test
        @DisplayName("deadline 이내에 끝나면 finished_at 이 기록된다")
        void recordsFinishedAt_whenFinishedWithinDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            boolean finished = generation.finish(CREATED_AT.plusSeconds(10));

            assertThat(finished).isTrue();
            assertThat(generation.getFinishedAt()).isEqualTo(CREATED_AT.plusSeconds(10));
        }

        @Test
        @DisplayName("deadline 정각에 끝나면 이내로 인정된다")
        void recordsFinishedAt_whenFinishedExactlyAtDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            boolean finished = generation.finish(DEADLINE);

            assertThat(finished).isTrue();
            assertThat(generation.getFinishedAt()).isEqualTo(DEADLINE);
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
        @DisplayName("작업이 끝나지 않고 deadline 이내면 진행 중으로 파생된다")
        void derivesInProgress_whenNotFinishedWithinDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.status(CREATED_AT.plusSeconds(10), List.of()))
                .isEqualTo(GenerationStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("결과가 다 찼어도 작업이 끝나지 않았으면 진행 중으로 파생된다")
        void derivesInProgress_whenAllChannelsSucceededBeforeFinish() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.status(CREATED_AT.plusSeconds(10), List.of(succeeded(Channel.BLOG))))
                .isEqualTo(GenerationStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("끝난 작업의 선택 채널이 전부 성공했으면 성공으로 파생된다")
        void derivesSucceeded_whenEveryChannelSucceeded() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM));
            generation.finish(CREATED_AT.plusSeconds(10));

            assertThat(generation.status(CREATED_AT.plusSeconds(10),
                List.of(succeeded(Channel.BLOG), succeeded(Channel.INSTAGRAM))))
                .isEqualTo(GenerationStatus.SUCCEEDED);
        }

        @Test
        @DisplayName("끝난 작업의 채널 하나가 실패했으면 실패로 파생된다")
        void derivesFailed_whenOneChannelFailed() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM));
            generation.finish(CREATED_AT.plusSeconds(10));

            assertThat(generation.status(CREATED_AT.plusSeconds(10),
                List.of(succeeded(Channel.BLOG), failed(Channel.INSTAGRAM))))
                .isEqualTo(GenerationStatus.FAILED);
        }

        @Test
        @DisplayName("끝난 작업에 결과 행이 없는 채널이 있으면 실패로 파생된다")
        void derivesFailed_whenChannelLeftUnrecorded() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM));
            generation.finish(CREATED_AT.plusSeconds(10));

            assertThat(generation.status(CREATED_AT.plusSeconds(10), List.of(succeeded(Channel.BLOG))))
                .isEqualTo(GenerationStatus.FAILED);
        }

        @Test
        @DisplayName("끝나지 않은 채 deadline 이 지나면 결과가 다 찼어도 실패로 파생된다")
        void derivesFailed_whenDeadlinePassedWithoutFinish() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.status(DEADLINE.plusSeconds(1), List.of(succeeded(Channel.BLOG))))
                .isEqualTo(GenerationStatus.FAILED);
        }

        @Test
        @DisplayName("deadline 정각에는 아직 진행 중으로 파생된다")
        void derivesInProgress_whenExactlyAtDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.status(DEADLINE, List.of())).isEqualTo(GenerationStatus.IN_PROGRESS);
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
    @DisplayName("channelResults")
    class ChannelResults {

        @Test
        @DisplayName("작업이 성공하면 채널별 제목·본문·해시태그가 실린다")
        void carriesContentPerChannel_whenSucceeded() {
            Generation generation = generation(List.of(Channel.BLOG));
            generation.finish(CREATED_AT.plusSeconds(10));

            List<ChannelResult> results = generation.channelResults(
                CREATED_AT.plusSeconds(10), List.of(succeeded(Channel.BLOG)));

            assertThat(results).singleElement().satisfies(result -> {
                assertThat(result.channel()).isEqualTo(Channel.BLOG);
                assertThat(result.title()).isEqualTo("제목");
                assertThat(result.body()).isEqualTo("본문");
                assertThat(result.hashtags()).containsExactly("#태그");
            });
        }

        @Test
        @DisplayName("작업이 진행 중이면 먼저 끝난 채널이 있어도 결과가 비어 있다")
        void staysEmpty_whenInProgress() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM));

            List<ChannelResult> results = generation.channelResults(
                CREATED_AT.plusSeconds(10), List.of(succeeded(Channel.BLOG)));

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("채널 하나가 실패하면 성공한 채널의 결과도 비어 있다")
        void staysEmpty_whenOneChannelFailed() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM));
            generation.finish(CREATED_AT.plusSeconds(10));

            List<ChannelResult> results = generation.channelResults(CREATED_AT.plusSeconds(10),
                List.of(succeeded(Channel.BLOG), failed(Channel.INSTAGRAM)));

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("결과가 다 찼어도 deadline 을 넘겨 끝나지 못하면 결과가 비어 있다")
        void staysEmpty_whenDeadlinePassedWithoutFinish() {
            Generation generation = generation(List.of(Channel.BLOG));

            List<ChannelResult> results = generation.channelResults(
                DEADLINE.plusSeconds(1), List.of(succeeded(Channel.BLOG)));

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("채널별 결과는 선택한 순서대로 나온다")
        void keepsSelectedChannelOrder() {
            Generation generation = generation(List.of(Channel.INSTAGRAM, Channel.BLOG, Channel.THREADS));
            generation.finish(CREATED_AT.plusSeconds(10));

            List<ChannelResult> results = generation.channelResults(CREATED_AT.plusSeconds(10),
                List.of(succeeded(Channel.BLOG), succeeded(Channel.THREADS), succeeded(Channel.INSTAGRAM)));

            assertThat(results).extracting(ChannelResult::channel)
                .containsExactly(Channel.INSTAGRAM, Channel.BLOG, Channel.THREADS);
        }
    }
}
