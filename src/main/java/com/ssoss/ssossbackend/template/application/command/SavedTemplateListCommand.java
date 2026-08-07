package com.ssoss.ssossbackend.template.application.command;

import com.ssoss.ssossbackend.shared.paging.CreatedAtSort;

import org.springframework.util.StringUtils;

public record SavedTemplateListCommand(Long memberId, CreatedAtSort sort, int page, int size) {

    public static SavedTemplateListCommand of(Long memberId, String sort, int page, int size) {
        return new SavedTemplateListCommand(
            memberId,
            StringUtils.hasText(sort) ? CreatedAtSort.from(sort) : CreatedAtSort.LATEST,
            page,
            size);
    }
}
