package com.ssoss.ssossbackend.content.domain.service;

import java.time.Clock;

import com.ssoss.ssossbackend.content.domain.contract.GenerationRepository;
import com.ssoss.ssossbackend.content.domain.contract.GenerationResultRepository;
import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.model.GenerationResultStatus;
import com.ssoss.ssossbackend.content.domain.model.LlmCallReply;
import com.ssoss.ssossbackend.content.event.GenerationResultSucceededEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationWriter {

    private final GenerationRepository generationRepository;
    private final GenerationResultRepository generationResultRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public Generation create(Generation generation) {
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
        if (status != GenerationResultStatus.SUCCEEDED) {
            generationResultRepository.save(GenerationResult.failed(generation.getId(), channel, status, reply));
            return;
        }
        GenerationResult succeeded = generationResultRepository.save(
            GenerationResult.succeeded(generation.getId(), channel, reply));
        eventPublisher.publishEvent(new GenerationResultSucceededEvent(generation.getMemberId(), succeeded.getId()));
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
