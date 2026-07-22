package com.ssoss.ssossbackend.content.infrastructure.ai;

import java.util.List;

record TitledGenerationOutput(String title, String body, List<String> hashtags) {
}
