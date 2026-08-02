package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

public record GenerationMaterial(
    Channel channel,
    Purpose purpose,
    Tone tone,
    String emphasis,
    String forbidden,
    List<String> keywords,
    boolean photoGuideChecked,
    StoreMaterial store
) {

    public GenerationMaterial {
        keywords = keywords == null ? List.of() : List.copyOf(keywords);
    }
}
