package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.model.PhotoGuideTag;

import org.springframework.stereotype.Component;

@Component
class ParagraphJoiner {

    private static final String PARAGRAPH_BREAK = "\n\n";
    private static final String MARKER_BREAK = "\n";

    String join(List<String> paragraphs) {
        StringBuilder joined = new StringBuilder();
        String previous = null;
        for (String paragraph : paragraphs) {
            if (previous != null) {
                joined.append(PhotoGuideTag.holdsOnlyMarkers(previous) || PhotoGuideTag.holdsOnlyMarkers(paragraph)
                    ? MARKER_BREAK : PARAGRAPH_BREAK);
            }
            joined.append(paragraph);
            previous = paragraph;
        }
        return joined.toString();
    }
}
