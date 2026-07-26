package com.ssoss.ssossbackend.content.application.result;

import java.util.List;

public record ContentDetailResult(
    Long contentId,
    String purpose,
    String tone,
    List<String> keywords,
    List<ContentChannelResult> contents
) {
}
