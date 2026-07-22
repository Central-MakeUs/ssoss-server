package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.ArrayList;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.GenerationMaterial;

import org.springframework.stereotype.Component;

@Component
class GenerationPromptComposer {

    private static final String ROLE_INSTRUCTION = """
        너는 소상공인 매장의 홍보 콘텐츠를 대신 써 주는 전문 카피라이터다.
        아래 지시를 모두 지켜 콘텐츠를 한국어로 작성한다.""";

    private static final String BLOG_INSTRUCTION = """
        [채널]
        네이버 블로그에 올릴 글을 쓴다.
        검색 노출을 고려한 30자 이내의 제목을 함께 쓰고, 본문은 1,000~2,000자 분량으로 문단을 나눠 자세히 쓴다.""";

    private static final String INSTAGRAM_INSTRUCTION = """
        [채널]
        인스타그램 피드에 올릴 캡션을 쓴다.
        제목 없이 본문만 쓰고, 첫 문장으로 시선을 끌며 300~800자 분량으로 쓴다. 줄바꿈으로 호흡을 나누고 이모지를 적절히 섞는다.""";

    private static final String DAANGN_BIZ_INSTRUCTION = """
        [채널]
        당근 비즈프로필 소식에 올릴 글을 쓴다.
        제목 없이 본문만 쓰고, 동네 이웃에게 말을 거는 친근한 문장으로 200~600자 분량으로 쓴다.""";

    private static final String THREADS_INSTRUCTION = """
        [채널]
        스레드에 올릴 게시물을 쓴다.
        제목 없이 본문만 쓰고, 대화하듯 짧고 편한 문장으로 100~400자 분량으로 쓴다.""";

    private static final String EMPHASIS_SECTION = """
        [강조 내용]
        아래 내용이 콘텐츠의 중심이 되도록 반드시 반영한다.
        %s""";

    private static final String FORBIDDEN_SECTION = """
        [금지 내용]
        아래 내용은 콘텐츠 어디에도 언급하지 않는다.
        %s""";

    private static final String KEYWORDS_SECTION = """
        [키워드]
        아래 키워드를 본문에 자연스럽게 녹인다.
        %s""";

    private static final String NO_STORE_SECTION = """
        [매장 정보]
        매장 정보가 제공되지 않았다. 매장명·업종·위치·메뉴 같은 매장에 대한 사실을 지어내지 않는다.""";

    private static final String HASHTAG_SECTION = """
        [해시태그]
        해시태그는 3~5개 만들고, 각 태그는 #으로 시작하는 공백 없는 한 단어로 쓴다.""";

    String compose(GenerationMaterial material) {
        String channelInstruction = switch (material.channel()) {
            case BLOG -> BLOG_INSTRUCTION;
            case INSTAGRAM -> INSTAGRAM_INSTRUCTION;
            case DAANGN_BIZ -> DAANGN_BIZ_INSTRUCTION;
            case THREADS -> THREADS_INSTRUCTION;
        };
        String purposeInstruction = switch (material.purpose()) {
            case INFORMATION -> "정보성 — 매장과 관련된 유용한 정보를 알려 주는 글을 쓴다.";
            case EVENT_DISCOUNT -> "이벤트/할인 — 이벤트·할인 소식을 알려 방문을 이끄는 글을 쓴다.";
            case NEW_MENU_PROMOTION -> "신메뉴/홍보 — 신메뉴나 매장의 매력을 알리는 홍보 글을 쓴다.";
        };
        String toneInstruction = switch (material.tone()) {
            case CASUAL -> "일상형 — 친구에게 말하듯 편안하고 자연스러운 말투로 쓴다.";
            case EMOTIONAL -> "감성형 — 감성적이고 따뜻한 분위기의 말투로 쓴다.";
            case INFORMATIVE -> "정보형 — 사실을 차분하게 전달하는 신뢰감 있는 말투로 쓴다.";
            case PROMOTIONAL -> "홍보형 — 혜택과 매력을 적극적으로 알리는 말투로 쓴다.";
        };
        List<String> sections = new ArrayList<>();
        sections.add(ROLE_INSTRUCTION);
        sections.add(channelInstruction);
        sections.add("[목적]\n" + purposeInstruction);
        sections.add("[톤]\n" + toneInstruction);
        sections.add(EMPHASIS_SECTION.formatted(material.emphasis()));
        if (material.forbidden() != null && !material.forbidden().isBlank()) {
            sections.add(FORBIDDEN_SECTION.formatted(material.forbidden()));
        }
        if (material.keywords() != null && !material.keywords().isBlank()) {
            sections.add(KEYWORDS_SECTION.formatted(material.keywords()));
        }
        sections.add(NO_STORE_SECTION);
        sections.add(HASHTAG_SECTION);
        return String.join("\n\n", sections);
    }
}
