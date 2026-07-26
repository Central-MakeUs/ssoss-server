package com.ssoss.ssossbackend.content.application.result;

import java.util.regex.Pattern;

import com.ssoss.ssossbackend.content.domain.model.ContentChannel;

public record ContentListTitle(String value) {

    private static final Pattern PHOTO_GUIDE_TAG = Pattern.compile("</?photo-guide(?=[\\s/>])[^>]*>");
    private static final Pattern WHITESPACE_RUN = Pattern.compile("\\s+");
    private static final int LIMIT = 20;
    private static final String ELLIPSIS = "…";

    public static ContentListTitle of(ContentChannel channel) {
        String source = WHITESPACE_RUN
            .matcher(channel.getChannel().hasTitle()
                ? channel.getTitle()
                : PHOTO_GUIDE_TAG.matcher(channel.getBody()).replaceAll(""))
            .replaceAll(" ")
            .strip();
        return new ContentListTitle(source.codePointCount(0, source.length()) <= LIMIT
            ? source
            : source.substring(0, source.offsetByCodePoints(0, LIMIT)) + ELLIPSIS);
    }
}
