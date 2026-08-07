package com.ssoss.ssossbackend.template.domain.service;

import com.ssoss.ssossbackend.template.domain.contract.TemplateRepository;
import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.model.TemplateCategory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateFinder {

    private static final Sort LATEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final TemplateRepository templateRepository;

    public Page<Template> list(TemplateCategory category, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, LATEST_FIRST);
        if (category == null) {
            return templateRepository.findAll(pageRequest);
        }
        return templateRepository.findAllByCategory(category, pageRequest);
    }
}
