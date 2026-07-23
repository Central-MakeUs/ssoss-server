package com.ssoss.ssossbackend.content.application.service;

import java.time.Clock;
import java.util.List;

import com.ssoss.ssossbackend.content.application.command.GenerationStartCommand;
import com.ssoss.ssossbackend.content.application.result.GenerationPollResult;
import com.ssoss.ssossbackend.content.application.result.GenerationStartResult;
import com.ssoss.ssossbackend.content.domain.model.Channel;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationFailureReason;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.model.GenerationStatus;
import com.ssoss.ssossbackend.content.domain.service.GenerationCoordinator;
import com.ssoss.ssossbackend.content.domain.service.GenerationFinder;
import com.ssoss.ssossbackend.content.domain.service.GenerationValidator;
import com.ssoss.ssossbackend.content.domain.service.GenerationWriter;
import com.ssoss.ssossbackend.credit.application.service.CreditService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerationService {

    private final GenerationValidator generationValidator;
    private final GenerationWriter generationWriter;
    private final GenerationCoordinator generationCoordinator;
    private final GenerationFinder generationFinder;
    private final CreditService creditService;
    private final Clock clock;

    @Transactional
    public GenerationStartResult start(GenerationStartCommand command) {
        generationValidator.ensureStartable(command.memberId());
        creditService.checkDeductible(command.memberId(), command.channels().size());
        Generation generation = generationWriter.create(Generation.create(
            command.memberId(), command.channels(), command.purpose(), command.tone(),
            command.emphasis(), command.forbidden(), command.keywords()));
        generationCoordinator.run(generation);
        return new GenerationStartResult(generation.getId());
    }

    public GenerationPollResult poll(Long generationId, Long memberId) {
        Generation generation = generationFinder.get(generationId, memberId);
        List<GenerationResult> results = generationFinder.results(generationId);
        List<Channel> settledChannels = results.stream()
            .map(GenerationResult::getChannel)
            .toList();
        List<GenerationResult> succeededResults = results.stream()
            .filter(GenerationResult::isSucceeded)
            .toList();
        List<Channel> succeededChannels = succeededResults.stream()
            .map(GenerationResult::getChannel)
            .toList();
        GenerationStatus status = generation.status(clock.instant(), succeededChannels);
        String failureReason = status != GenerationStatus.FAILED ? null
            : GenerationFailureReason.derive(results)
                .map(Enum::name)
                .orElse(null);
        return new GenerationPollResult(
            generation.getId(),
            status.name(),
            failureReason,
            succeededResults.stream()
                .map(result -> new GenerationPollResult.ChannelResult(
                    result.getChannel().name(), result.getTitle(), result.getBody(), result.hashtagList()))
                .toList(),
            generation.pendingChannels(settledChannels).stream()
                .map(Channel::name)
                .toList());
    }
}
