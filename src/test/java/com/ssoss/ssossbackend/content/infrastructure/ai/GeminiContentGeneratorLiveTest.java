package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.GenerationMaterial;
import com.ssoss.ssossbackend.content.domain.model.LlmCallReply;
import com.ssoss.ssossbackend.content.domain.model.Purpose;
import com.ssoss.ssossbackend.content.domain.model.Tone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("live")
@DisplayName("Gemini 라이브")
class GeminiContentGeneratorLiveTest {

    private GeminiContentGenerator generator() {
        Client client = Client.builder()
            .apiKey(geminiApiKey())
            .httpOptions(HttpOptions.builder().timeout(50_000).build())
            .build();
        GoogleGenAiChatModel chatModel = GoogleGenAiChatModel.builder()
            .genAiClient(client)
            .options(GoogleGenAiChatOptions.builder().model("gemini-3.1-flash-lite").build())
            .build();
        return new GeminiContentGenerator(chatModel, new GenerationPromptComposer(),
            new GeminiCallOutcomeClassifier());
    }

    static List<Channel> titledChannels() {
        return Arrays.stream(Channel.values()).filter(Channel::hasTitle).toList();
    }

    static List<Channel> untitledChannels() {
        return Arrays.stream(Channel.values()).filter(channel -> !channel.hasTitle()).toList();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("titledChannels")
    @DisplayName("제목 있는 채널을 실호출하면 제목·본문·해시태그가 스키마대로 생성된다")
    void generatesTitledContent_withRealGeminiCall(Channel channel) {
        LlmCallReply reply = generator().generate(new GenerationMaterial(
            channel, Purpose.EVENT_DISCOUNT, Tone.CASUAL,
            "이번 주말 아메리카노 1+1 이벤트", "가격 인상 언급", List.of("동네 카페")));

        print(channel, reply);
        assertThat(reply.content().hasRequiredOutput(channel)).isTrue();
        assertThat(reply.content().title()).isNotBlank();
        assertThat(reply.content().body()).isNotBlank();
        assertThat(reply.content().hashtags()).isNotEmpty();
        assertThat(reply.outputTokens()).isPositive();
        assertThat(reply.responseTimeMillis()).isPositive();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("untitledChannels")
    @DisplayName("제목 없는 채널을 실호출하면 제목 없이 본문·해시태그가 생성된다")
    void generatesUntitledContent_withRealGeminiCall(Channel channel) {
        LlmCallReply reply = generator().generate(new GenerationMaterial(
            channel, Purpose.INFORMATION, Tone.EMOTIONAL,
            "가을 신메뉴 밤라떼 출시", null, List.of()));

        print(channel, reply);
        assertThat(reply.content().hasRequiredOutput(channel)).isTrue();
        assertThat(reply.content().title()).isNull();
        assertThat(reply.content().body()).isNotBlank();
        assertThat(reply.content().hashtags()).isNotEmpty();
        assertThat(reply.outputTokens()).isPositive();
        assertThat(reply.responseTimeMillis()).isPositive();
    }

    private String geminiApiKey() {
        String fromEnv = System.getenv("GEMINI_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        try {
            return Files.readAllLines(Path.of(".env")).stream()
                .filter(line -> line.startsWith("GEMINI_API_KEY="))
                .map(line -> line.substring("GEMINI_API_KEY=".length()).trim())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("GEMINI_API_KEY 가 환경변수나 .env 에 없습니다"));
        } catch (IOException e) {
            throw new IllegalStateException("GEMINI_API_KEY 가 환경변수나 .env 에 없습니다", e);
        }
    }

    private void print(Channel channel, LlmCallReply reply) {
        System.out.println("""

            ===== %s 생성 결과 =====
            제목: %s
            본문:
            %s
            해시태그: %s
            (입력 %d 토큰 / 출력 %d 토큰 / %d ms)
            """.formatted(channel, reply.content().title(), reply.content().body(),
            String.join(" ", reply.content().hashtags()),
            reply.inputTokens(), reply.outputTokens(), reply.responseTimeMillis()));
    }
}
