package com.ssoss.ssossbackend.content.application.service;

import com.ssoss.ssossbackend.content.application.command.GenerationStartCommand;
import com.ssoss.ssossbackend.content.application.result.GenerationStartResult;
import com.ssoss.ssossbackend.content.domain.model.Generation;
import com.ssoss.ssossbackend.content.domain.service.GenerationCoordinator;
import com.ssoss.ssossbackend.content.domain.service.GenerationWriter;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerationService {

    private final GenerationWriter generationWriter;
    private final GenerationCoordinator generationCoordinator;

    public GenerationStartResult start(GenerationStartCommand command) {
        Generation generation = generationWriter.create(Generation.create(
            command.memberId(), command.channels(), command.purpose(), command.tone(),
            command.emphasis(), command.forbidden(), command.keywords()));
        generationCoordinator.run(generation);
        return new GenerationStartResult(generation.getId());
    }
}
