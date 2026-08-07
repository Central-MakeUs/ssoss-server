package com.ssoss.ssossbackend.template.application.result;

import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;

public record SavedTemplateSaveResult(Long savedTemplateId) {

    public static SavedTemplateSaveResult from(SavedTemplate savedTemplate) {
        return new SavedTemplateSaveResult(savedTemplate.getId());
    }
}
