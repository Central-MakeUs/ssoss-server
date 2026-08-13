package com.ssoss.ssossbackend.content.domain.service;

import com.ssoss.ssossbackend.content.domain.contract.CreditClient;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditDeductor {

    private final CreditClient creditClient;

    public void checkDeductible(Long memberId, int channelCount) {
        creditClient.checkDeductible(memberId, channelCount);
    }

    public void deduct(Long memberId, Long generationId, int channelCount, String description) {
        creditClient.deduct(memberId, generationId, channelCount, description);
    }
}
