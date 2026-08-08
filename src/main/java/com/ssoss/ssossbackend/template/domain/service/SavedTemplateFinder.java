package com.ssoss.ssossbackend.template.domain.service;

import com.ssoss.ssossbackend.shared.exception.BusinessException;
import com.ssoss.ssossbackend.shared.paging.CreatedAtSort;
import com.ssoss.ssossbackend.template.domain.contract.SavedTemplateRepository;
import com.ssoss.ssossbackend.template.domain.model.SavedTemplate;
import com.ssoss.ssossbackend.template.domain.model.TemplateErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavedTemplateFinder {

    private final SavedTemplateRepository savedTemplateRepository;

    public Page<SavedTemplate> list(Long memberId, CreatedAtSort sort, int page, int size) {
        return savedTemplateRepository.findAllByMemberIdAndDeletedAtIsNull(
            memberId, PageRequest.of(page, size, sort.order()));
    }

    public SavedTemplate get(Long savedTemplateId, Long memberId) {
        return savedTemplateRepository.findByIdAndMemberIdAndDeletedAtIsNull(savedTemplateId, memberId)
            .orElseThrow(() -> new BusinessException(TemplateErrorCode.SAVED_TEMPLATE_NOT_FOUND));
    }
}
