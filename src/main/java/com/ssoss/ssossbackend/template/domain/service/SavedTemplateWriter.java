package com.ssoss.ssossbackend.template.domain.service;

import java.util.List;

import com.ssoss.ssossbackend.template.domain.contract.SavedTemplateHistoryRepository;
import com.ssoss.ssossbackend.template.domain.contract.SavedTemplateRepository;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplateHistory;
import com.ssoss.ssossbackend.template.domain.model.Template;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedTemplateWriter {

    private final SavedTemplateRepository savedTemplateRepository;
    private final SavedTemplateHistoryRepository savedTemplateHistoryRepository;

    public SavedTemplate save(Template template, Long memberId, String body) {
        return savedTemplateRepository.save(SavedTemplate.copyOf(template, memberId, body));
    }

    @Transactional
    public SavedTemplate edit(SavedTemplate savedTemplate, String title, String body) {
        SavedTemplateHistory previous = SavedTemplateHistory.previousOf(savedTemplate);
        if (!savedTemplate.edit(title, body)) {
            return savedTemplate;
        }
        savedTemplateHistoryRepository.save(previous);
        return savedTemplateRepository.save(savedTemplate);
    }

    @Transactional
    public void deleteAllByMemberId(Long memberId) {
        List<Long> savedTemplateIds = savedTemplateRepository.findAllByMemberId(memberId).stream()
            .map(SavedTemplate::getId)
            .toList();
        if (!savedTemplateIds.isEmpty()) {
            savedTemplateHistoryRepository.deleteAllBySavedTemplateIdIn(savedTemplateIds);
        }
        savedTemplateRepository.deleteAllByMemberId(memberId);
    }
}
