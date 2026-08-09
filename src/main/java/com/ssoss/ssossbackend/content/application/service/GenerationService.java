package com.ssoss.ssossbackend.content.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.ssoss.ssossbackend.content.application.command.GenerationStartCommand;
import com.ssoss.ssossbackend.content.application.command.StyleReuseCommand;
import com.ssoss.ssossbackend.content.application.result.GenerationDetailResult;
import com.ssoss.ssossbackend.content.application.result.GenerationStartResult;
import com.ssoss.ssossbackend.content.domain.model.ContentChannel;
import com.ssoss.ssossbackend.content.domain.model.ContentWithChannels;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.model.StyleSource;
import com.ssoss.ssossbackend.content.domain.service.ContentFinder;
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

    private static final int REUSE_CHANNEL_COUNT = 1;

    private final GenerationValidator generationValidator;
    private final GenerationWriter generationWriter;
    private final GenerationCoordinator generationCoordinator;
    private final GenerationFinder generationFinder;
    private final ContentFinder contentFinder;
    private final CreditService creditService;
    private final StoreMaterialReader storeMaterialReader;
    private final Clock clock;

    @Transactional
    public GenerationStartResult start(GenerationStartCommand command) {
        generationValidator.ensureStartable(command.memberId());
        creditService.checkDeductible(command.memberId(), command.channels().size());
        Generation generation = generationWriter.create(Generation.create(
            command.memberId(), command.channels(), command.purpose(), command.tone(),
            command.emphasis(), command.forbidden(), command.keywords(), command.photoGuideChecked()));
        generationCoordinator.run(generation, storeMaterialReader.read(command.memberId()), StyleSource.none());
        return new GenerationStartResult(generation.getId());
    }

    @Transactional
    public GenerationStartResult reuse(StyleReuseCommand command) {
        ContentWithChannels origin = contentFinder.get(command.contentId(), command.memberId());
        ContentChannel originChannel = origin.channelOf(command.contentChannelId());
        generationValidator.ensureStartable(command.memberId());
        creditService.checkDeductible(command.memberId(), REUSE_CHANNEL_COUNT);
        Generation generation = generationWriter.create(Generation.reuseOf(origin.content(), originChannel,
            command.emphasis(), command.forbidden(), command.keywords(), command.photoGuideChecked()));
        generationCoordinator.run(generation, storeMaterialReader.read(command.memberId()),
            StyleSource.of(originChannel));
        return new GenerationStartResult(generation.getId());
    }

    public GenerationDetailResult getById(Long generationId, Long memberId) {
        Instant now = clock.instant();
        Generation generation = generationFinder.get(generationId, memberId);
        List<GenerationResult> results = generationFinder.results(generationId);
        return new GenerationDetailResult(
            generation.getId(),
            generation.status(now, results).name(),
            generation.getPurpose().name(),
            generation.getTone().name(),
            generation.keywordList(),
            generation.channelResults(now, results).stream()
                .map(result -> new GenerationDetailResult.ChannelDetail(
                    result.channel().name(), result.title(), result.body(), result.hashtags()))
                .toList());
    }
}
