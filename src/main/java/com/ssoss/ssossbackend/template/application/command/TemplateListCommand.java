package com.ssoss.ssossbackend.template.application.command;

import com.ssoss.ssossbackend.template.domain.model.TemplateCategory;

import org.springframework.util.StringUtils;

public record TemplateListCommand(Long memberId, TemplateCategory category, String keyword, int page, int size) {

    public static TemplateListCommand of(Long memberId, String category, String keyword, int page, int size) {
        return new TemplateListCommand(
            memberId,
            StringUtils.hasText(category) ? TemplateCategory.from(category) : null,
            StringUtils.hasText(keyword) ? keyword.strip() : null,
            page,
            size);
    }
}
