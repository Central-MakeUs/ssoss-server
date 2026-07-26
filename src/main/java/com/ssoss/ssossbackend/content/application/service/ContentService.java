package com.ssoss.ssossbackend.content.application.service;

import java.util.List;

import com.ssoss.ssossbackend.content.application.command.ContentSaveCommand;
import com.ssoss.ssossbackend.content.application.result.ContentSaveResult;
import com.ssoss.ssossbackend.content.domain.model.ContentErrorCode;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.model.GenerationResult;
import com.ssoss.ssossbackend.content.domain.service.ContentWriter;
import com.ssoss.ssossbackend.content.domain.service.GenerationFinder;
import com.ssoss.ssossbackend.content.domain.service.GenerationValidator;
import com.ssoss.ssossbackend.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final GenerationFinder generationFinder;
    private final GenerationValidator generationValidator;
    private final ContentWriter contentWriter;

    public ContentSaveResult save(ContentSaveCommand command) {
        Generation generation = generationFinder.get(command.generationId(), command.memberId());
        generationValidator.ensureCompleted(generation);
        List<GenerationResult> succeeded = generationFinder.results(generation.getId()).stream()
            .filter(GenerationResult::isSucceeded)
            .toList();
        if (succeeded.isEmpty()) {
            throw new BusinessException(ContentErrorCode.NO_SAVABLE_GENERATION_RESULT);
        }
        return new ContentSaveResult(contentWriter.save(generation.getMemberId(), succeeded).stream()
            .map(content -> new ContentSaveResult.Item(
                content.getId(), content.getGenerationResultId(), content.getChannel().name()))
            .toList());
    }
}
