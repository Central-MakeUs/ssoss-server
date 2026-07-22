package com.ssoss.ssossbackend.content.domain.service;

import java.util.List;

import com.ssoss.ssossbackend.content.domain.contract.GenerationRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerationFinder {

    private final GenerationRepository generationRepository;
    private final GenerationResultRepository generationResultRepository;

    public Generation get(Long generationId, Long memberId) {
        return generationRepository.findByIdAndMemberId(generationId, memberId)
            .orElseThrow(() -> new BusinessException(ContentErrorCode.GENERATION_NOT_FOUND));
    }

    public List<GenerationResult> results(Long generationId) {
        return generationResultRepository.findAllByGenerationIdOrderById(generationId);
    }
}
