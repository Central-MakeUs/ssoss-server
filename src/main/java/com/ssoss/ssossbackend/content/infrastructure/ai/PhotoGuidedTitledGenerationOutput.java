package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;

record PhotoGuidedTitledGenerationOutput(
    String title,
    List<String> paragraphs,
    List<String> hashtags,
    List<PhotoGuideOutput> photoGuides
) {
}
