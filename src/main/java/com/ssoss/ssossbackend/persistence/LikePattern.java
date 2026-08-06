package com.ssoss.ssossbackend.persistence;

public final class LikePattern {

    public static final String ESCAPE = "!";

    private LikePattern() {
    }

    public static String forPartialMatch(String keyword) {
        String escaped = keyword
            .replace(ESCAPE, ESCAPE + ESCAPE)
            .replace("%", ESCAPE + "%")
            .replace("_", ESCAPE + "_");
        return "%" + escaped + "%";
    }
}
