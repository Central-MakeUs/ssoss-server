package com.ssoss.ssossbackend.template.application.service;

import com.ssoss.ssossbackend.shared.paging.PagedResult;
import com.ssoss.ssossbackend.template.application.command.SavedTemplateListCommand;
import com.ssoss.ssossbackend.template.application.command.SavedTemplateSaveCommand;
import com.ssoss.ssossbackend.template.application.result.SavedTemplateDetailResult;
import com.ssoss.ssossbackend.template.application.result.SavedTemplateSaveResult;
import com.ssoss.ssossbackend.template.application.result.SavedTemplateSummaryResult;
import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.service.SavedTemplateFinder;
import com.ssoss.ssossbackend.template.domain.service.SavedTemplateWriter;
import com.ssoss.ssossbackend.template.domain.service.TemplateFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavedTemplateService {

    private final TemplateFinder templateFinder;
    private final SavedTemplateFinder savedTemplateFinder;
    private final SavedTemplateWriter savedTemplateWriter;

    public SavedTemplateSaveResult save(SavedTemplateSaveCommand command) {
        Template template = templateFinder.get(command.templateId());
        return SavedTemplateSaveResult.from(
            savedTemplateWriter.save(template, command.memberId(), command.body()));
    }

    public PagedResult<SavedTemplateSummaryResult> list(SavedTemplateListCommand command) {
        return PagedResult.from(savedTemplateFinder.list(
                command.memberId(), command.sort(), command.page(), command.size()),
            SavedTemplateSummaryResult::from);
    }

    public SavedTemplateDetailResult getById(Long savedTemplateId, Long memberId) {
        return SavedTemplateDetailResult.from(savedTemplateFinder.get(savedTemplateId, memberId));
    }

    public void deleteAllByMemberId(Long memberId) {
        savedTemplateWriter.deleteAllByMemberId(memberId);
    }
}
