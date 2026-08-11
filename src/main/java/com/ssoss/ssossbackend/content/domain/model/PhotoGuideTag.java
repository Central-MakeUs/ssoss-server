package com.ssoss.ssossbackend.content.domain.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PhotoGuideTag {

    private static final Pattern PATTERN = Pattern.compile("</?photo-guide(?=[\\s/>])[^>]*>");

    private PhotoGuideTag() {
    }

    public static Matcher markersIn(String body) {
        return PATTERN.matcher(body);
    }

    public static String removeFrom(String body) {
        return PATTERN.matcher(body).replaceAll("");
    }

    public static boolean holdsOnlyMarkers(String body) {
        return removeFrom(body).isBlank();
    }
}
