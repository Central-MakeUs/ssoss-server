package com.ssoss.ssossbackend.content.event;

public record GenerationSucceededEvent(Long memberId, Long generationId, int channelCount,
                                       String deductionDescription) {
}
