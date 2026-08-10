package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.ArrayList;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.GenerationMaterial;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class GenerationPromptComposer {

    private static final String ROLE_INSTRUCTION = """
        너는 소상공인 매장의 홍보 콘텐츠를 대신 써 주는 전문 카피라이터다.
        아래 지시를 모두 지켜 콘텐츠를 한국어로 작성한다.""";

    private static final String BLOG_INSTRUCTION = """
        [채널]
        네이버 블로그에 올릴 글을 쓴다.
        검색 노출을 고려한 40자 이내의 제목을 함께 쓴다.
        본문은 1,000자 이상 2,000자 이하로 쓴다. 1,000자에 못 미치는 본문은 지시를 어긴 것이다.
        문단은 4~6개로 나누고 각 문단을 200자 이상으로 쓴다.""";

    private static final String INSTAGRAM_INSTRUCTION = """
        [채널]
        인스타그램 피드에 올릴 캡션을 쓴다.
        제목 없이 본문만 쓰고, 첫 문장으로 시선을 끈다.
        본문은 300자 이상 700자 이하로 쓴다.
        문단은 2~4개로 나누고 이모지를 적절히 섞는다.""";

    private static final String DAANGN_BIZ_INSTRUCTION = """
        [채널]
        당근 비즈프로필 소식에 올릴 글을 쓴다.
        제목 없이 본문만 쓰고, 동네 이웃에게 말을 거는 친근한 문장으로 쓴다.
        본문은 150자 이상 400자 이하로 쓴다.
        문단은 2~3개로 나눈다.""";

    private static final String THREADS_INSTRUCTION = """
        [채널]
        스레드에 올릴 게시물을 쓴다.
        제목 없이 본문만 쓰고, 대화하듯 짧고 편한 문장으로 쓴다.
        본문은 100자 이상 500자 이하로 쓴다.
        문단은 2~3개로 나눈다.""";

    private static final String BODY_FORMAT_SECTION = """
        [본문 형식]
        본문은 paragraphs 배열에 담고, 원소 하나가 문단 하나다.
        원소 안에서는 줄바꿈을 쓰지 않고 빈 원소도 넣지 않는다.""";

    private static final String EMPHASIS_SECTION = """
        [강조 내용]
        아래 내용이 콘텐츠의 중심이 되도록 반드시 반영한다.
        매장과 무관해 보이는 내용이라도 [매장 정보]에 적힌 사실 안에서만 매장을 알리는 이야기로 연결해 풀어낸다.
        %s""";

    private static final String FORBIDDEN_SECTION = """
        [금지 내용]
        아래 내용은 콘텐츠 어디에도 언급하지 않는다.
        %s""";

    private static final String KEYWORDS_SECTION = """
        [키워드]
        아래 키워드를 본문에 자연스럽게 녹인다.
        %s""";

    private static final String PHOTO_GUIDE_SECTION = """
        [사진 가이드]
        사진이 들어가면 좋을 자리를 %s 골라, 그 자리에 <photo-guide/> 마커만 담은 원소를 문단 사이에 끼운다.
        마커와 같은 순서로 photoGuides 배열을 채우고 개수를 정확히 맞춘다.
        title 은 어떤 사진인지 15자 이내로, description 은 어떻게 찍으면 좋은지 40자 이내로 쓴다.
        마커와 배열 외의 방법으로 사진을 언급하지 않는다.""";

    private static final String HASHTAG_SECTION = """
        [해시태그]
        해시태그는 10개 만들고, 각 태그는 #으로 시작하는 공백 없는 한 단어로 쓴다.""";

    private static final String OPTIONAL_HASHTAG_SECTION = """
        [해시태그]
        해시태그는 어울릴 때만 최대 3개까지 만들고, 어울리지 않으면 만들지 않는다.
        만들 때는 #으로 시작하는 공백 없는 한 단어로 쓴다.""";

    private static final String NO_HASHTAG_SECTION = """
        [해시태그]
        해시태그를 만들지 않는다. 본문에도 #으로 시작하는 태그를 쓰지 않는다.""";

    private final StoreSectionComposer storeSectionComposer;
    private final StyleSourceSectionComposer styleSourceSectionComposer;

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
        sections.add(BODY_FORMAT_SECTION);
        sections.add("[목적]\n" + purposeInstruction);
        sections.add("[톤]\n" + toneInstruction);
        sections.add(EMPHASIS_SECTION.formatted(material.emphasis()));
        if (material.forbidden() != null && !material.forbidden().isBlank()) {
            sections.add(FORBIDDEN_SECTION.formatted(material.forbidden()));
        }
        if (!material.keywords().isEmpty()) {
            sections.add(KEYWORDS_SECTION.formatted(String.join(", ", material.keywords())));
        }
        sections.add(storeSectionComposer.compose(material.store()));
        if (!material.styleSource().isEmpty()) {
            sections.addAll(styleSourceSectionComposer.compose(material.styleSource()));
        }
        if (material.photoGuideChecked()) {
            String places = switch (material.channel()) {
                case BLOG -> "2~4곳";
                case INSTAGRAM -> "1~2곳";
                case DAANGN_BIZ, THREADS -> "1곳";
            };
            sections.add(PHOTO_GUIDE_SECTION.formatted(places));
        }
        sections.add(switch (material.channel()) {
            case BLOG, INSTAGRAM -> HASHTAG_SECTION;
            case THREADS -> OPTIONAL_HASHTAG_SECTION;
            case DAANGN_BIZ -> NO_HASHTAG_SECTION;
        });
        return String.join("\n\n", sections);
    }
}
