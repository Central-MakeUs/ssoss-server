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

    private LlmCallReply reply(GeneratedContent content) {
        return new LlmCallReply(content, 100L, 10, 20, "{}");
    }

    private Generation generation(List<Channel> channels) {
        Generation generation = Generation.create(1L, channels, Purpose.INFORMATION, Tone.CASUAL,
            "강조", null, null, false);
        return new Generation(1L, 1L, generation.getChannels(), generation.getPurpose(), generation.getTone(),
            generation.getEmphasis(), generation.getForbidden(), generation.getKeywords(),
            generation.isPhotoGuideChecked(), generation.getSourceContentId(), CREATED_AT, null);
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

            assertThat(generation.status(CREATED_AT.plusSeconds(10))).isEqualTo(GenerationStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("작업이 끝나면 완료로 파생된다")
        void derivesCompleted_whenFinished() {
            Generation generation = generation(List.of(Channel.BLOG));
            generation.finish(CREATED_AT.plusSeconds(10));

            assertThat(generation.status(CREATED_AT.plusSeconds(10))).isEqualTo(GenerationStatus.COMPLETED);
        }

        @Test
        @DisplayName("끝나지 않은 채 deadline 이 지나면 완료로 파생된다")
        void derivesCompleted_whenDeadlinePassedWithoutFinish() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.status(DEADLINE.plusSeconds(1))).isEqualTo(GenerationStatus.COMPLETED);
        }

        @Test
        @DisplayName("deadline 정각에는 아직 진행 중으로 파생된다")
        void derivesInProgress_whenExactlyAtDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            assertThat(generation.status(DEADLINE)).isEqualTo(GenerationStatus.IN_PROGRESS);
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
        @DisplayName("결과 행이 없고 deadline 이내면 진행 중으로 파생된다")
        void derivesPending_whenNoResultWithinDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            List<ChannelResult> results = generation.channelResults(CREATED_AT.plusSeconds(10), List.of());

            assertThat(results).singleElement().satisfies(result -> {
                assertThat(result.channel()).isEqualTo(Channel.BLOG);
                assertThat(result.status()).isEqualTo(ChannelStatus.PENDING);
                assertThat(result.message()).isEqualTo(ChannelOutcome.PENDING.getMessage());
                assertThat(result.title()).isNull();
                assertThat(result.body()).isNull();
                assertThat(result.hashtags()).isEmpty();
            });
        }

        @Test
        @DisplayName("성공한 결과 행이 있으면 성공으로 파생되고 콘텐츠가 실린다")
        void derivesSucceededWithContent_whenSucceededResultExists() {
            Generation generation = generation(List.of(Channel.BLOG));
            GenerationResult succeeded = GenerationResult.succeeded(1L, Channel.BLOG,
                reply(new GeneratedContent("제목", "본문", List.of("#태그"))));

            List<ChannelResult> results = generation.channelResults(CREATED_AT.plusSeconds(10), List.of(succeeded));

            assertThat(results).singleElement().satisfies(result -> {
                assertThat(result.status()).isEqualTo(ChannelStatus.SUCCEEDED);
                assertThat(result.message()).isEqualTo(ChannelOutcome.SUCCEEDED.getMessage());
                assertThat(result.title()).isEqualTo("제목");
                assertThat(result.body()).isEqualTo("본문");
                assertThat(result.hashtags()).containsExactly("#태그");
            });
        }

        @Test
        @DisplayName("실패한 결과 행이 있으면 실패로 파생되고 사유 문구가 붙는다")
        void derivesFailedWithReasonMessage_whenFailedResultExists() {
            Generation generation = generation(List.of(Channel.BLOG));
            GenerationResult failed = GenerationResult.failed(1L, Channel.BLOG,
                GenerationResultStatus.RATE_LIMITED, 100L, null, null, null);

            List<ChannelResult> results = generation.channelResults(CREATED_AT.plusSeconds(10), List.of(failed));

            assertThat(results).singleElement().satisfies(result -> {
                assertThat(result.status()).isEqualTo(ChannelStatus.FAILED);
                assertThat(result.message()).isEqualTo(ChannelOutcome.OVERLOADED.getMessage());
                assertThat(result.title()).isNull();
                assertThat(result.body()).isNull();
                assertThat(result.hashtags()).isEmpty();
            });
        }

        @Test
        @DisplayName("결과 행 없이 deadline 이 지나면 시간 초과로 실패 파생된다")
        void derivesTimedOut_whenNoResultAfterDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            List<ChannelResult> results = generation.channelResults(DEADLINE.plusSeconds(1), List.of());

            assertThat(results).singleElement().satisfies(result -> {
                assertThat(result.status()).isEqualTo(ChannelStatus.FAILED);
                assertThat(result.message()).isEqualTo(ChannelOutcome.TIMED_OUT.getMessage());
            });
        }

        @Test
        @DisplayName("작업이 끝났는데 결과 행이 없는 채널은 deadline 전이라도 시간 초과로 실패 파생된다")
        void derivesTimedOut_whenFinishedWithoutResult() {
            Generation generation = generation(List.of(Channel.BLOG));
            generation.finish(CREATED_AT.plusSeconds(10));

            List<ChannelResult> results = generation.channelResults(CREATED_AT.plusSeconds(10), List.of());

            assertThat(results).singleElement().satisfies(result -> {
                assertThat(result.status()).isEqualTo(ChannelStatus.FAILED);
                assertThat(result.message()).isEqualTo(ChannelOutcome.TIMED_OUT.getMessage());
            });
        }

        @Test
        @DisplayName("결과 행이 없어도 deadline 정각까지는 진행 중으로 파생된다")
        void derivesPending_whenExactlyAtDeadline() {
            Generation generation = generation(List.of(Channel.BLOG));

            List<ChannelResult> results = generation.channelResults(DEADLINE, List.of());

            assertThat(results).singleElement().satisfies(result ->
                assertThat(result.status()).isEqualTo(ChannelStatus.PENDING));
        }

        @Test
        @DisplayName("작업이 끝나면 결과 행이 있는 채널과 없는 채널이 각각 성공과 실패로 갈린다")
        void splitsSucceededAndUnrecordedChannels_whenFinished() {
            Generation generation = generation(List.of(Channel.BLOG, Channel.INSTAGRAM));
            GenerationResult succeeded = GenerationResult.succeeded(1L, Channel.BLOG,
                reply(new GeneratedContent("제목", "본문", List.of("#태그"))));
            generation.finish(CREATED_AT.plusSeconds(10));

            List<ChannelResult> results = generation.channelResults(
                CREATED_AT.plusSeconds(10), List.of(succeeded));

            assertThat(results).satisfiesExactly(
                blog -> {
                    assertThat(blog.channel()).isEqualTo(Channel.BLOG);
                    assertThat(blog.status()).isEqualTo(ChannelStatus.SUCCEEDED);
                    assertThat(blog.body()).isEqualTo("본문");
                },
                instagram -> {
                    assertThat(instagram.channel()).isEqualTo(Channel.INSTAGRAM);
                    assertThat(instagram.status()).isEqualTo(ChannelStatus.FAILED);
                    assertThat(instagram.message()).isEqualTo(ChannelOutcome.TIMED_OUT.getMessage());
                    assertThat(instagram.body()).isNull();
                });
        }

        @Test
        @DisplayName("채널별 결과는 선택한 순서대로 나온다")
        void keepsSelectedChannelOrder() {
            Generation generation = generation(List.of(Channel.INSTAGRAM, Channel.BLOG, Channel.THREADS));

            List<ChannelResult> results = generation.channelResults(CREATED_AT.plusSeconds(10), List.of());

            assertThat(results).extracting(ChannelResult::channel)
                .containsExactly(Channel.INSTAGRAM, Channel.BLOG, Channel.THREADS);
        }
    }
}
