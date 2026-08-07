package com.ssoss.ssossbackend.template.domain.service;

import com.ssoss.ssossbackend.template.domain.contract.SavedTemplateRepository;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;
import com.ssoss.ssossbackend.template.domain.model.Template;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavedTemplateWriter {

    private final SavedTemplateRepository savedTemplateRepository;

    public SavedTemplate save(Template template, Long memberId, String body) {
        return savedTemplateRepository.save(SavedTemplate.copyOf(template, memberId, body));
    }

    public void deleteAllByMemberId(Long memberId) {
        savedTemplateRepository.deleteAllByMemberId(memberId);
    }
}
