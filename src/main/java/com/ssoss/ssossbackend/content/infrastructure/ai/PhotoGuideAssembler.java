package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.ssoss.ssossbackend.content.domain.model.PhotoGuideTag;

import org.springframework.stereotype.Component;

@Component
class PhotoGuideAssembler {

    private static final String TAG = "<photo-guide title=\"%s\" description=\"%s\"/>";
    private static final String PARAGRAPH_BREAK = "\n\n";

    String assemble(List<String> paragraphs, List<PhotoGuideOutput> photoGuides) {
        if (paragraphs == null) {
            return null;
        }
        String body = paragraphs.stream()
            .filter(paragraph -> paragraph != null && !paragraph.isBlank())
            .collect(Collectors.joining(PARAGRAPH_BREAK));
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
