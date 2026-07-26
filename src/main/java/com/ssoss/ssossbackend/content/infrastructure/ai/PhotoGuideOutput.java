package com.ssoss.ssossbackend.content.infrastructure.ai;

record PhotoGuideOutput(String title, String description) {

    boolean isComplete() {
        return title != null && !title.isBlank() && description != null && !description.isBlank();
    }
}
