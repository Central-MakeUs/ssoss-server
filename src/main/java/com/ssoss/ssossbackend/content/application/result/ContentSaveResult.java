package com.ssoss.ssossbackend.content.application.result;

import java.util.List;

public record ContentSaveResult(List<Item> contents) {

    public record Item(Long contentId, Long generationResultId, String channel) {
    }
}
