package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;

record PhotoGuidedTitledGenerationOutput(
    String title,
    String body,
    List<String> hashtags,
    List<PhotoGuideOutput> photoGuides
) {
}
