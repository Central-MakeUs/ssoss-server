package com.ssoss.ssossbackend.template.application.command;

import com.ssoss.ssossbackend.template.domain.model.TemplateCategory;

import org.springframework.util.StringUtils;

public record TemplateListCommand(TemplateCategory category, int page, int size) {

    public static TemplateListCommand of(String category, int page, int size) {
        return new TemplateListCommand(
            StringUtils.hasText(category) ? TemplateCategory.from(category) : null,
            page,
            size);
    }
}
