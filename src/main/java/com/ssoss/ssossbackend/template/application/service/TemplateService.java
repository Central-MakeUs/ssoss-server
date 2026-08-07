package com.ssoss.ssossbackend.template.application.service;

import com.ssoss.ssossbackend.shared.paging.PagedResult;
import com.ssoss.ssossbackend.store.application.service.StoreProfile;
import com.ssoss.ssossbackend.store.application.service.StoreService;
import com.ssoss.ssossbackend.template.application.command.TemplateListCommand;
import com.ssoss.ssossbackend.template.application.result.TemplateAppliedResult;
import com.ssoss.ssossbackend.template.application.result.TemplateDetailResult;
import com.ssoss.ssossbackend.template.application.result.TemplateResult;
import com.ssoss.ssossbackend.template.domain.model.StoreInfo;
import com.ssoss.ssossbackend.template.domain.model.StoreOperatingHours;
import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.service.TemplateFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateFinder templateFinder;
    private final StoreService storeService;

    public PagedResult<TemplateResult> list(TemplateListCommand command) {
        return PagedResult.from(templateFinder.list(command.category(), command.page(), command.size()),
            TemplateResult::from);
    }

    public TemplateDetailResult getById(Long templateId) {
        return TemplateDetailResult.from(templateFinder.get(templateId));
    }

    public TemplateAppliedResult apply(Long templateId, Long memberId) {
        Template template = templateFinder.get(templateId);
        StoreProfile profile = storeService.getProfile(memberId);
        return TemplateAppliedResult.from(template, new StoreInfo(profile.name(), profile.address(),
            new StoreOperatingHours(profile.businessDays(), profile.openTime(), profile.closeTime())));
    }
}
