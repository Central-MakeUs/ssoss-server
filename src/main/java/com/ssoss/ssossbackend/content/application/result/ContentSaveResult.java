package com.ssoss.ssossbackend.content.application.result;

import java.util.List;

public record ContentSaveResult(Long contentId, List<Item> contents) {

    public record Item(Long contentChannelId, String channel) {
    }
}
