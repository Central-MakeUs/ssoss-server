package com.ssoss.ssossbackend.content.entrypoint.listener;

import com.ssoss.ssossbackend.content.event.GenerationResultSucceededEvent;
import com.ssoss.ssossbackend.credit.application.service.CreditService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerationCreditListener {

    private final CreditService creditService;

    @EventListener
    public void deductCredit(GenerationResultSucceededEvent event) {
        creditService.deduct(event.memberId(), event.generationResultId());
    }
}
