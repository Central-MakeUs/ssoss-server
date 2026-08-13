package com.ssoss.ssossbackend.content.domain.contract;

public interface CreditClient {

    void checkDeductible(Long memberId, int channelCount);

    void deduct(Long memberId, Long generationId, int channelCount, String description);
}
