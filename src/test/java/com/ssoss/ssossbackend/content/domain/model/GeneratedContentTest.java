package com.ssoss.ssossbackend.content.domain.model;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeneratedContent")
class GeneratedContentTest {

    @Nested
    @DisplayName("hasRequiredOutput")
    class HasRequiredOutput {

        @Test
        @DisplayName("블로그는 제목과 본문이 모두 있어야 산출로 인정된다")
        void requiresTitleAndBody_forBlog() {
            GeneratedContent content = new GeneratedContent("제목", "본문", List.of("#태그"));

            assertThat(content.hasRequiredOutput(Channel.BLOG)).isTrue();
        }

        @Test
        @DisplayName("블로그 제목이 비면 산출로 인정되지 않는다")
        void rejectsBlankTitle_forBlog() {
            GeneratedContent content = new GeneratedContent(" ", "본문", List.of("#태그"));

            assertThat(content.hasRequiredOutput(Channel.BLOG)).isFalse();
        }

        @Test
        @DisplayName("제목 없는 채널은 본문만 있으면 산출로 인정된다")
        void requiresOnlyBody_forUntitledChannel() {
            GeneratedContent content = new GeneratedContent(null, "본문", List.of("#태그"));

            assertThat(content.hasRequiredOutput(Channel.INSTAGRAM)).isTrue();
        }

        @Test
        @DisplayName("본문이 비면 채널과 무관하게 산출로 인정되지 않는다")
        void rejectsBlankBody_forAnyChannel() {
            GeneratedContent content = new GeneratedContent(null, " ", List.of("#태그"));

            assertThat(content.hasRequiredOutput(Channel.THREADS)).isFalse();
        }
    }
}
