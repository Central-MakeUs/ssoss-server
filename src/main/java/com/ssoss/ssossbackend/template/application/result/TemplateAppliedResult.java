package com.ssoss.ssossbackend.template.application.result;

import com.ssoss.ssossbackend.template.domain.model.StoreInfo;
import com.ssoss.ssossbackend.template.domain.model.Template;

public record TemplateAppliedResult(Long id, String body) {

    public static TemplateAppliedResult from(Template template, StoreInfo storeInfo) {
        return new TemplateAppliedResult(template.getId(), template.replacePlaceholders(storeInfo));
    }
}
