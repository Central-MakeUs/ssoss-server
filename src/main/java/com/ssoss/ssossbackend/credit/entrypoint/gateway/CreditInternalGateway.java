package com.ssoss.ssossbackend.credit.entrypoint.gateway;

import com.ssoss.ssossbackend.credit.application.service.CreditService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreditInternalGateway {

    private final CreditService creditService;

    public void checkDeductible(Long memberId, int channelCount) {
        creditService.checkDeductible(memberId, channelCount);
    }

    public void deduct(Long memberId, Long generationId, int channelCount, String description) {
        creditService.deduct(memberId, generationId, channelCount, description);
    }
}
