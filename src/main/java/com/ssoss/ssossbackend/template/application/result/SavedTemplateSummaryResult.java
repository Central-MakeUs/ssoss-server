package com.ssoss.ssossbackend.template.application.result;

import java.time.Instant;

import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;

public record SavedTemplateSummaryResult(Long savedTemplateId, String category, String title, String description,
                                         Instant savedAt) {

    public static SavedTemplateSummaryResult from(SavedTemplate savedTemplate) {
        return new SavedTemplateSummaryResult(savedTemplate.getId(), savedTemplate.getCategory().name(),
            savedTemplate.getTitle(), savedTemplate.getDescription(), savedTemplate.getCreatedAt());
    }
}
