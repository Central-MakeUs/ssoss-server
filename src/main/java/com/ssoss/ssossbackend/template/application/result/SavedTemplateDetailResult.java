package com.ssoss.ssossbackend.template.application.result;

import java.time.Instant;
import java.util.List;

import com.ssoss.ssossbackend.template.domain.model.Channel;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;

public record SavedTemplateDetailResult(Long savedTemplateId, String category, String title, String description,
                                        String body, List<String> recommendedChannels, Instant savedAt) {

    public static SavedTemplateDetailResult from(SavedTemplate savedTemplate) {
        return new SavedTemplateDetailResult(savedTemplate.getId(), savedTemplate.getCategory().name(),
            savedTemplate.getTitle(), savedTemplate.getDescription(), savedTemplate.getBody(),
            savedTemplate.recommendedChannelList().stream().map(Channel::name).toList(),
            savedTemplate.getCreatedAt());
    }
}
