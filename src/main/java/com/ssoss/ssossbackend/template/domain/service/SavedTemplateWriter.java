package com.ssoss.ssossbackend.template.domain.service;

import java.time.Clock;
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
    private final Clock clock;

    public SavedTemplate save(Template template, Long memberId, String body) {
        return savedTemplateRepository.save(SavedTemplate.copyOf(template, memberId, body));
    }

    @Transactional
    public SavedTemplate edit(SavedTemplate savedTemplate, String body) {
        SavedTemplateHistory previous = SavedTemplateHistory.previousOf(savedTemplate);
        if (!savedTemplate.edit(body)) {
            return savedTemplate;
        }
        savedTemplateHistoryRepository.save(previous);
        return savedTemplateRepository.save(savedTemplate);
    }

    @Transactional
    public SavedTemplate rename(SavedTemplate savedTemplate, String title) {
        SavedTemplateHistory previous = SavedTemplateHistory.previousOf(savedTemplate);
        if (!savedTemplate.rename(title)) {
            return savedTemplate;
        }
        savedTemplateHistoryRepository.save(previous);
        return savedTemplateRepository.save(savedTemplate);
    }

    public void delete(SavedTemplate savedTemplate) {
        savedTemplate.delete(clock.instant());
        savedTemplateRepository.save(savedTemplate);
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
