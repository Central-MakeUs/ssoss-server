package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.ArrayList;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.StyleSource;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class StyleSourceSectionComposer {

    private static final String HEADER = """
        [참고 글]
        아래는 회원이 전에 만들어 둔 글이다. 새 글의 재료가 아니라 문체를 보여주는 예시다.""";

    private static final String TITLE_LINE = "제목: %s";

    private static final String BODY_LINES = """
        본문:
        %s""";

    private static final String SCOPE_SECTION = """
        [참고 범위]
        참고 글에서 가져오는 것은 말투와 문장 길이뿐이다.
        줄과 문단을 나누는 방식은 [본문 형식]과 [채널]을 따르고, 참고 글의 모양은 따르지 않는다.
        참고 글에 나온 메뉴·소식·이벤트·장소 같은 소재는 새 글에 옮기지 않는다.
        새 글의 소재는 [강조 내용]에 적힌 것에서만 가져오고, [강조 내용]에 없는 소재는 참고 글에 있어도 쓰지 않는다.""";

    private static final String LINE_BREAK = "\n";

    List<String> compose(StyleSource styleSource) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        if (StringUtils.hasText(styleSource.title())) {
            lines.add(TITLE_LINE.formatted(styleSource.title()));
        }
        lines.add(BODY_LINES.formatted(styleSource.body()));
        return List.of(String.join(LINE_BREAK, lines), SCOPE_SECTION);
    }
}
