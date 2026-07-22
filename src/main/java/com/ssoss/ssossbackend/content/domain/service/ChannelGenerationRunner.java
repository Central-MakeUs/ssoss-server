package com.ssoss.ssossbackend.content.domain.service;

import java.time.Duration;

import com.ssoss.ssossbackend.content.domain.contract.ContentGenerator;
import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResultStatus;
import com.ssoss.ssossbackend.content.domain.model.LlmCallFailedException;
import com.ssoss.ssossbackend.content.domain.model.LlmCallReply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelGenerationRunner {

    private final ContentGenerator contentGenerator;
    private final GenerationWriter generationWriter;

    public void run(Generation generation, Channel channel) {
        long startedAtNanos = System.nanoTime();
        try {
            LlmCallReply reply = contentGenerator.generate(generation.materialFor(channel));
            GenerationResultStatus status = reply.content().hasRequiredOutput(channel)
                ? GenerationResultStatus.SUCCEEDED
                : GenerationResultStatus.EMPTY_OUTPUT;
            generationWriter.settle(generation, channel, status, reply);
        } catch (LlmCallFailedException e) {
            GenerationResultStatus status = Thread.interrupted() ? GenerationResultStatus.TIMEOUT : e.getOutcome();
            log.warn("채널 생성 호출이 실패했습니다: generationId={}, channel={}, status={}",
                generation.getId(), channel, status, e);
            generationWriter.settleFailure(generation, channel, status,
                e.getResponseTimeMillis(), e.getInputTokens(), e.getOutputTokens(), e.getRawResponse());
        } catch (Exception e) {
            boolean cancelled = Thread.interrupted();
            log.warn("채널 생성에 실패했습니다: generationId={}, channel={}", generation.getId(), channel, e);
            if (cancelled) {
                generationWriter.settleFailure(generation, channel, GenerationResultStatus.TIMEOUT,
                    Duration.ofNanos(System.nanoTime() - startedAtNanos).toMillis(), null, null, null);
            }
        }
    }
}
