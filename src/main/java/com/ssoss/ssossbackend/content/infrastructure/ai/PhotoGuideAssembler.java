package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;
import java.util.regex.Matcher;

import com.ssoss.ssossbackend.content.domain.model.PhotoGuideTag;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PhotoGuideAssembler {

    private static final String TAG = "<photo-guide title=\"%s\" description=\"%s\"/>";

    private final ParagraphJoiner paragraphJoiner;

    String assemble(List<String> paragraphs, List<PhotoGuideOutput> photoGuides) {
        if (paragraphs == null) {
            return null;
        }
        String body = paragraphJoiner.join(paragraphs.stream()
            .filter(paragraph -> paragraph != null && !paragraph.isBlank())
            .toList());
        List<PhotoGuideOutput> guides = photoGuides == null ? List.of() : photoGuides;
        Matcher matcher = PhotoGuideTag.markersIn(body);
        StringBuilder assembled = new StringBuilder();
        int paired = 0;
        while (matcher.find()) {
            if (matcher.group().startsWith("</")) {
                matcher.appendReplacement(assembled, "");
                continue;
            }
            PhotoGuideOutput guide = paired < guides.size() ? guides.get(paired) : null;
            String replacement = guide != null && guide.isComplete()
                ? TAG.formatted(new TagAttribute(guide.title()), new TagAttribute(guide.description()))
                : "";
            matcher.appendReplacement(assembled, Matcher.quoteReplacement(replacement));
            paired++;
        }
        matcher.appendTail(assembled);
        return assembled.toString();
    }
}
