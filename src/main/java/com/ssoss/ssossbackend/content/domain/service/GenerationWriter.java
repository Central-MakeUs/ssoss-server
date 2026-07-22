package com.ssoss.ssossbackend.content.domain.service;

import java.time.Clock;

import com.ssoss.ssossbackend.content.domain.contract.GenerationLockRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationLock;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.model.GenerationResultStatus;
import com.ssoss.ssossbackend.content.domain.model.LlmCallReply;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationWriter {

    private final GenerationRepository generationRepository;
    private final GenerationResultRepository generationResultRepository;
    private final GenerationLockRepository generationLockRepository;
    private final Clock clock;

    @Transactional
    public Generation create(Generation generation) {
        Long memberId = generation.getMemberId();
        if (!generationLockRepository.existsByMemberId(memberId)) {
            try {
                generationLockRepository.save(GenerationLock.create(memberId));
            } catch (DuplicateKeyException raced) {
            }
        }
        generationLockRepository.findByMemberId(memberId);
        if (generationRepository.existsByMemberIdAndFinishedAtIsNullAndCreatedAtGreaterThanEqual(
            memberId, clock.instant().minus(Generation.DEADLINE))) {
            throw new BusinessException(ContentErrorCode.GENERATION_IN_PROGRESS_EXISTS);
        }
        return generationRepository.save(generation);
    }

    @Transactional
    public void settle(Generation generation, Channel channel, GenerationResultStatus status, LlmCallReply reply) {
        if (status == GenerationResultStatus.SUCCEEDED && generation.isExpired(clock.instant())) {
            log.warn("deadline 을 넘긴 지각 결과를 폐기합니다: generationId={}, channel={}", generation.getId(), channel);
            generationResultRepository.save(GenerationResult.failed(generation.getId(), channel,
                GenerationResultStatus.DISCARDED_LATE, reply));
            return;
        }
        generationResultRepository.save(status == GenerationResultStatus.SUCCEEDED
            ? GenerationResult.succeeded(generation.getId(), channel, reply)
            : GenerationResult.failed(generation.getId(), channel, status, reply));
    }

    @Transactional
    public void settleFailure(Generation generation, Channel channel, GenerationResultStatus status,
        long responseTimeMillis, Integer inputTokens, Integer outputTokens, String rawResponse) {
        generationResultRepository.save(GenerationResult.failed(generation.getId(), channel, status,
            responseTimeMillis, inputTokens, outputTokens, rawResponse));
    }

    @Transactional
    public void finish(Generation generation) {
        if (generation.finish(clock.instant())) {
            generationRepository.save(generation);
        }
    }
}
