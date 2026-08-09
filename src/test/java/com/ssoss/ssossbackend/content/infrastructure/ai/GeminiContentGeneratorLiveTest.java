package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.GenerationMaterial;
import com.ssoss.ssossbackend.content.domain.model.LlmCallReply;
import com.ssoss.ssossbackend.content.domain.model.Purpose;
import com.ssoss.ssossbackend.content.domain.model.StoreMaterial;
import com.ssoss.ssossbackend.content.domain.model.StyleSource;
import com.ssoss.ssossbackend.content.domain.model.Tone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("live")
@DisplayName("Gemini 라이브")
class GeminiContentGeneratorLiveTest {

    private static final Pattern PHOTO_GUIDE_TAG = Pattern.compile("<photo-guide(?=[\\s/>])[^>]*>");
    private static final Pattern PARAGRAPH_BREAK = Pattern.compile("\\n\\s*\\n");

    private final Channel channel = Channel.from(System.getProperty("live.channel", "BLOG"));

    @Test
    @DisplayName("한 채널을 실호출하면 채널 정책대로 생성되고 분량·개수가 실측된다")
    void generatesContentWithinChannelPolicy_withRealGeminiCall() {
        LlmCallReply reply = generator().generate(new GenerationMaterial(
            channel, Purpose.NEW_MENU_PROMOTION, Tone.CASUAL,
            "가을 신메뉴 밤라떼 출시", "가격 인상 언급", List.of("동네 카페"), true,
            new StoreMaterial("보니스커피", "카페", "서울 중구 을지로 100", "을지로 크루아상 카페",
                List.of("월요일", "화요일"), "09:00", "22:00", List.of("크루아상"), List.of("포장 가능")),
            StyleSource.none()));

        print(channel, reply);
        assertThat(reply.content().hasRequiredOutput(channel)).isTrue();
        assertThat(reply.content().body())
            .contains("<photo-guide title=")
            .doesNotContain("<photo-guide/>")
            .doesNotContain("</photo-guide>");
        if (channel == Channel.DAANGN_BIZ) {
            assertThat(reply.content().hashtags()).isEmpty();
        }
        assertThat(reply.outputTokens()).isPositive();
        assertThat(reply.responseTimeMillis()).isPositive();
    }

    @Test
    @DisplayName("참고 글을 넣어 실호출하면 소재가 새 강조 내용으로 갈리고 참고 글의 소재는 따라오지 않는다")
    void replacesSubjectWithNewEmphasis_whenStyleSourceGiven() {
        LlmCallReply reply = generator().generate(new GenerationMaterial(
            Channel.BLOG, Purpose.NEW_MENU_PROMOTION, Tone.CASUAL,
            "이번 주 새로 나온 파니니 출시", null, List.of(), false,
            new StoreMaterial("보니스커피", "카페", "서울 중구 을지로 100", "을지로 베이커리 카페",
                List.of("월요일", "화요일"), "09:00", "22:00", List.of("파니니"), List.of("포장 가능")),
            new StyleSource("을지로 크루아상 맛집 | 겹겹이 살아있는 결, 보니스커피", """
                을지로에서 크루아상 하나를 제대로 먹고 싶다면, 보니스커피를 추천드려요.

                매일 아침 6시부터 반죽을 밀어 겹겹이 결을 살린 크루아상은 겉은 바삭하고 속은 촉촉합니다.
                버터 향이 은은하게 올라와서 커피 한 잔과 곁들이면 아침이 달라집니다.

                을지로 나들이 길에 크루아상 한 봉지 들고 가 보세요.""")));

        print(Channel.BLOG, reply);
        assertThat(reply.content().hasRequiredOutput(Channel.BLOG)).isTrue();
        assertThat(reply.content().body()).contains("파니니").doesNotContain("크루아상");
    }

    private GeminiContentGenerator generator() {
        Client client = Client.builder()
            .apiKey(geminiApiKey())
            .httpOptions(HttpOptions.builder().timeout(50_000).build())
            .build();
        GoogleGenAiChatModel chatModel = GoogleGenAiChatModel.builder()
            .genAiClient(client)
            .options(GoogleGenAiChatOptions.builder().model("gemini-3.1-flash-lite").build())
            .build();
        return new GeminiContentGenerator(chatModel,
            new GenerationPromptComposer(new StoreSectionComposer(), new StyleSourceSectionComposer()),
            new GeminiCallOutcomeClassifier(), new PhotoGuideAssembler());
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
        String body = reply.content().body();
        String stripped = PHOTO_GUIDE_TAG.matcher(body).replaceAll("").strip();
        String title = reply.content().title();
        System.out.println("""

            ===== %s 실측 =====
            제목: %s (%d자)
            본문: %d자 (사진 가이드 태그 제외) / 태그 포함 %d자
            문단: %d개
            사진 가이드: %d장
            해시태그: %d개 — %s
            (입력 %d 토큰 / 출력 %d 토큰 / %d ms)

            %s
            """.formatted(channel,
            title, title == null ? 0 : title.codePointCount(0, title.length()),
            stripped.codePointCount(0, stripped.length()), body.codePointCount(0, body.length()),
            PARAGRAPH_BREAK.split(stripped).length,
            PHOTO_GUIDE_TAG.matcher(body).results().count(),
            reply.content().hashtags().size(), String.join(" ", reply.content().hashtags()),
            reply.inputTokens(), reply.outputTokens(), reply.responseTimeMillis(),
            body));
    }
}
