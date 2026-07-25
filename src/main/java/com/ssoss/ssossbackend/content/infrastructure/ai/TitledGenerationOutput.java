package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;

record TitledGenerationOutput(String title, List<String> paragraphs, List<String> hashtags) {
}
