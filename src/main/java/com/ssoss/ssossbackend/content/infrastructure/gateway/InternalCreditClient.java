package com.ssoss.ssossbackend.content.infrastructure.gateway;

import com.ssoss.ssossbackend.content.domain.contract.CreditClient;
import com.ssoss.ssossbackend.credit.entrypoint.gateway.CreditInternalGateway;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class InternalCreditClient implements CreditClient {

    private final CreditInternalGateway creditInternalGateway;

    @Override
    public void checkDeductible(Long memberId, int channelCount) {
        creditInternalGateway.checkDeductible(memberId, channelCount);
    }

    @Override
    public void deduct(Long memberId, Long generationId, int channelCount, String description) {
        creditInternalGateway.deduct(memberId, generationId, channelCount, description);
    }
}
