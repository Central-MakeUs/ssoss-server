package com.ssoss.ssossbackend.template.application.service;

import com.ssoss.ssossbackend.template.application.command.SavedTemplateSaveCommand;
import com.ssoss.ssossbackend.template.application.result.SavedTemplateSaveResult;
import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.service.SavedTemplateWriter;
import com.ssoss.ssossbackend.template.domain.service.TemplateFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavedTemplateService {

    private final TemplateFinder templateFinder;
    private final SavedTemplateWriter savedTemplateWriter;

    public SavedTemplateSaveResult save(SavedTemplateSaveCommand command) {
        Template template = templateFinder.get(command.templateId());
        return SavedTemplateSaveResult.from(
            savedTemplateWriter.save(template, command.memberId(), command.body()));
    }

    public void deleteAllByMemberId(Long memberId) {
        savedTemplateWriter.deleteAllByMemberId(memberId);
    }
}
