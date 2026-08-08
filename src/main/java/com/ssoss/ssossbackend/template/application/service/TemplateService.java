package com.ssoss.ssossbackend.template.application.service;

import java.util.List;
import java.util.Set;

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
import com.ssoss.ssossbackend.template.domain.service.TemplateBookmarkWriter;
import com.ssoss.ssossbackend.template.domain.service.TemplateFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateFinder templateFinder;
    private final TemplateBookmarkWriter templateBookmarkWriter;
    private final StoreService storeService;

    public PagedResult<TemplateResult> list(TemplateListCommand command) {
        Page<Template> found = templateFinder.list(command.category(), command.page(), command.size());
        Set<Long> bookmarkedIds = templateFinder.findBookmarkedIds(command.memberId(),
            found.getContent().stream().map(Template::getId).toList());
        return PagedResult.from(found,
            template -> TemplateResult.from(template, bookmarkedIds.contains(template.getId())));
    }

    public void bookmark(Long memberId, Long templateId) {
        templateBookmarkWriter.bookmark(templateFinder.get(templateId), memberId);
    }

    public void unbookmark(Long memberId, Long templateId) {
        templateBookmarkWriter.unbookmark(memberId, templateId);
    }

    public TemplateDetailResult getById(Long templateId, Long memberId) {
        Template template = templateFinder.get(templateId);
        return TemplateDetailResult.from(template,
            templateFinder.findBookmarkedIds(memberId, List.of(templateId)).contains(templateId));
    }

    public TemplateAppliedResult apply(Long templateId, Long memberId) {
        Template template = templateFinder.get(templateId);
        StoreProfile profile = storeService.getProfile(memberId);
        return TemplateAppliedResult.from(template, new StoreInfo(profile.name(), profile.address(),
            new StoreOperatingHours(profile.businessDays(), profile.openTime(), profile.closeTime())));
    }
}
