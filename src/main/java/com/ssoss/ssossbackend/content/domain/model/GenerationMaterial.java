package com.ssoss.ssossbackend.content.domain.model;

public record GenerationMaterial(
    Channel channel,
    Purpose purpose,
    Tone tone,
    String emphasis,
    String forbidden,
    String keywords
) {
}
