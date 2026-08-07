package com.ssoss.ssossbackend.template;

import java.util.List;

import com.ssoss.ssossbackend.support.IntegrationTest;
import com.ssoss.ssossbackend.template.domain.model.Template;
import com.ssoss.ssossbackend.template.domain.model.TemplateCategory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("추천 템플릿 시드")
class TemplateSeedTest extends IntegrationTest {

    @Nested
    @DisplayName("template 테이블")
    class SeededTemplates {

        @Test
        @DisplayName("분류마다 템플릿이 하나 이상 심겨 있다")
        void coversEveryCategory_whenMigrationApplied() {
            List<Template> templates = database.templates();

            assertThat(templates).extracting(Template::getCategory)
                .containsAll(List.of(TemplateCategory.values()));
        }

        @Test
        @DisplayName("본문에는 중괄호 자리표시자가 남아 있고 예시 본문에는 채워져 있다")
        void leavesPlaceholdersInBody_whenExampleBodyIsFilled() {
            List<Template> templates = database.templates();

            assertThat(templates).isNotEmpty();
            assertThat(templates).allSatisfy(template -> {
                assertThat(template.getBody()).contains("{").contains("}");
                assertThat(template.getExampleBody()).doesNotContain("{").doesNotContain("}");
            });
        }

        @Test
        @DisplayName("본문과 예시 본문의 줄바꿈이 글자 그대로가 아니라 실제 줄바꿈으로 심긴다")
        void seedsRealLineBreaks_whenMigrationApplied() {
            List<Template> templates = database.templates();

            assertThat(templates).allSatisfy(template -> {
                assertThat(template.getBody()).contains("\n").doesNotContain("\\n");
                assertThat(template.getExampleBody()).contains("\n").doesNotContain("\\n");
            });
        }

        @Test
        @DisplayName("추천 채널이 비어 있는 템플릿은 없다")
        void carriesAtLeastOneRecommendedChannel_whenMigrationApplied() {
            List<Template> templates = database.templates();

            assertThat(templates).allSatisfy(template ->
                assertThat(template.recommendedChannelList()).isNotEmpty());
        }
    }
}
