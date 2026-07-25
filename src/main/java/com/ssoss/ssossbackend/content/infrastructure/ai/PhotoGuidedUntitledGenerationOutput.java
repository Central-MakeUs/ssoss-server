package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;

record PhotoGuidedUntitledGenerationOutput(
    String body,
    List<String> hashtags,
    List<PhotoGuideOutput> photoGuides
) {
}
