package com.ssoss.ssossbackend.content.domain.service;

import java.time.Clock;
import java.util.List;

import com.ssoss.ssossbackend.content.domain.contract.GenerationLockRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationRepository;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.model.GenerationStatus;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerationValidator {

    private final GenerationRepository generationRepository;
    private final GenerationLockRepository generationLockRepository;
    private final Clock clock;

    public void ensureStartable(Long memberId) {
        generationLockRepository.acquire(memberId, clock.instant());
        if (generationRepository.existsByMemberIdAndFinishedAtIsNullAndCreatedAtGreaterThanEqual(
            memberId, clock.instant().minus(Generation.DEADLINE))) {
            throw new BusinessException(ContentErrorCode.GENERATION_IN_PROGRESS_EXISTS);
        }
    }

    public void ensureSavable(Generation generation, List<GenerationResult> results) {
        GenerationStatus status = generation.status(clock.instant(), results);
        if (status == GenerationStatus.IN_PROGRESS) {
            throw new BusinessException(ContentErrorCode.GENERATION_NOT_FINISHED);
        }
        if (status == GenerationStatus.FAILED) {
            throw new BusinessException(ContentErrorCode.GENERATION_FAILED);
        }
    }
}
