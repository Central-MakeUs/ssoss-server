package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
class PhotoGuideAssembler {

    private static final Pattern MARKER = Pattern.compile("</?photo-guide(?=[\\s/>])[^>]*>");
    private static final String TAG = "<photo-guide type=\"%s\" title=\"%s\" description=\"%s\"/>";
    private static final String PARAGRAPH_BREAK = "\n\n";

    String assemble(List<String> paragraphs, List<PhotoGuideOutput> photoGuides) {
        if (paragraphs == null) {
            return null;
        }
        String body = paragraphs.stream()
            .filter(paragraph -> paragraph != null && !paragraph.isBlank())
            .collect(Collectors.joining(PARAGRAPH_BREAK));
        List<PhotoGuideOutput> guides = photoGuides == null
            ? List.of()
            : photoGuides.stream().filter(guide -> guide != null && guide.type() != null).toList();
        Matcher matcher = MARKER.matcher(body);
        StringBuilder assembled = new StringBuilder();
        int paired = 0;
        while (matcher.find()) {
            if (matcher.group().startsWith("</")) {
                matcher.appendReplacement(assembled, "");
                continue;
            }
            String replacement = paired < guides.size()
                ? TAG.formatted(guides.get(paired).type(),
                    new TagAttribute(guides.get(paired).title()),
                    new TagAttribute(guides.get(paired).description()))
                : "";
            matcher.appendReplacement(assembled, Matcher.quoteReplacement(replacement));
            paired++;
        }
        matcher.appendTail(assembled);
        return assembled.toString();
    }
}
