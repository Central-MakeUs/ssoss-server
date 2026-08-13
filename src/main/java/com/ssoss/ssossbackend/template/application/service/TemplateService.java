package com.ssoss.ssossbackend.template.application.service;

import java.util.List;
import java.util.Set;

import com.ssoss.ssossbackend.shared.paging.PagedResult;
import com.ssoss.ssossbackend.template.application.command.TemplateListCommand;
import com.ssoss.ssossbackend.template.application.result.TemplateAppliedResult;
import com.ssoss.ssossbackend.template.application.result.TemplateDetailResult;
import com.ssoss.ssossbackend.template.application.result.TemplateResult;
import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.service.TemplateBookmarkWriter;
import com.ssoss.ssossbackend.template.domain.service.StoreInfoFinder;
import com.ssoss.ssossbackend.template.domain.service.TemplateFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateFinder templateFinder;
    private final TemplateBookmarkWriter templateBookmarkWriter;
    private final StoreInfoFinder storeInfoFinder;

    public PagedResult<TemplateResult> list(TemplateListCommand command) {
        Page<Template> found =
            templateFinder.list(command.category(), command.keyword(), command.page(), command.size());
        Set<Long> bookmarkedIds = templateFinder.findBookmarkedIds(command.memberId(),
            found.getContent().stream().map(Template::getId).toList());
        return PagedResult.from(found,
            template -> TemplateResult.from(template, bookmarkedIds.contains(template.getId())));
    }

    public List<TemplateResult> listBookmarked(Long memberId) {
        return templateFinder.listBookmarked(memberId).stream()
            .map(template -> TemplateResult.from(template, true))
            .toList();
    }

    public void bookmark(Long memberId, Long templateId) {
        templateBookmarkWriter.bookmark(templateFinder.get(templateId), memberId);
    }

    public void unbookmark(Long memberId, Long templateId) {
        templateBookmarkWriter.unbookmark(memberId, templateId);
    }

    public void deleteBookmarksByMemberId(Long memberId) {
        templateBookmarkWriter.deleteAllByMemberId(memberId);
    }

    public TemplateDetailResult getById(Long templateId, Long memberId) {
        Template template = templateFinder.get(templateId);
        return TemplateDetailResult.from(template,
            templateFinder.findBookmarkedIds(memberId, List.of(templateId)).contains(templateId));
    }

    public TemplateAppliedResult apply(Long templateId, Long memberId) {
        Template template = templateFinder.get(templateId);
        return TemplateAppliedResult.from(template, storeInfoFinder.get(memberId));
    }
}
