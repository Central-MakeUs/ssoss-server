package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;

record PhotoGuidedBodyOnlyGenerationOutput(
    List<String> paragraphs,
    List<PhotoGuideOutput> photoGuides
) {
}
