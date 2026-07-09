package com.ssoss.ssossbackend.architecture;

import com.ssoss.ssossbackend.SsossBackendApplication;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("모듈 구조 규칙 (Modulith 명문화)")
class ModuleStructureRulesTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ssoss.ssossbackend");

    private static final JavaClasses CYCLE_FIXTURES = new ClassFileImporter()
            .importPackages("com.ssoss.archfixtures.cycle");

    private static ArchRule freeOfCyclesIn(String sliceIdentifier) {
        return slices()
                .matching(sliceIdentifier)
                .should().beFreeOfCycles();
    }

    @Nested
    @DisplayName("애플리케이션 모듈 사이에 순환 의존이 없다")
    class Cycles {

        @Test
        @DisplayName("모듈이 서로 순환하지 않으면 통과한다")
        void productionCodePasses() {
            ArchRule rule = freeOfCyclesIn("com.ssoss.ssossbackend.(*)..");

            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("모듈이 서로를 의존해 순환이 생기면 실패한다")
        void violatingFixtureFails() {
            ArchRule rule = freeOfCyclesIn("com.ssoss.archfixtures.cycle.(*)..");

            assertThatThrownBy(() -> rule.check(CYCLE_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Cycle");
        }
    }

    @Nested
    @DisplayName("모듈 모델 명문화")
    class ModuleModel {

        @Test
        @DisplayName("shared 커널이 애플리케이션 모듈로 인식된다")
        void sharedModuleIsRecognized() {
            ApplicationModules modules = ApplicationModules.of(SsossBackendApplication.class);

            assertThat(modules.getModuleByName("shared")).isPresent();
        }
    }
}
