package com.ssoss.ssossbackend.template.application.service;

import com.ssoss.ssossbackend.template.application.command.TemplateListCommand;
import com.ssoss.ssossbackend.template.application.result.TemplateListResult;
import com.ssoss.ssossbackend.template.domain.service.TemplateFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateFinder templateFinder;

    public TemplateListResult list(TemplateListCommand command) {
        return TemplateListResult.from(templateFinder.list(command.category(), command.page(), command.size()));
    }
}
