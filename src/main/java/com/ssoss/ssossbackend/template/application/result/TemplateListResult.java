package com.ssoss.ssossbackend.template.application.result;

import java.util.List;

import com.ssoss.ssossbackend.template.domain.model.Template;

import org.springframework.data.domain.Page;

public record TemplateListResult(long totalCount, int page, int size, boolean hasNext,
                                 List<TemplateResult> templates) {

    public static TemplateListResult from(Page<Template> found) {
        return new TemplateListResult(found.getTotalElements(), found.getNumber(), found.getSize(), found.hasNext(),
            found.getContent().stream()
                .map(TemplateResult::from)
                .toList());
    }
}
