package com.ssoss.ssossbackend.content.entrypoint.listener;

import com.ssoss.ssossbackend.content.application.service.GenerationService;
import com.ssoss.ssossbackend.content.event.GenerationSucceededEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerationCreditListener {

    private final GenerationService generationService;

    @EventListener
    public void deductCredit(GenerationSucceededEvent event) {
        generationService.deductCredit(event.memberId(), event.generationId(), event.channelCount(),
            event.deductionDescription());
    }
}
