package com.ssoss.ssossbackend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("레이어/의존 방향 규칙")
class DependencyRulesTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ssoss.ssossbackend");

    private static final JavaClasses DIRECTION_FIXTURES = new ClassFileImporter()
            .importPackages("com.ssoss.archfixtures.direction");

    private static final JavaClasses LAYERING_FIXTURES = new ClassFileImporter()
            .importPackages("com.ssoss.archfixtures.layering");

    private static ArchRule mustNotDependOn(String fromPackage, String toPackage) {
        return noClasses()
                .that().resideInAPackage(fromPackage)
                .should().dependOnClassesThat().resideInAPackage(toPackage)
                .allowEmptyShould(true);
    }

    private static ArchRule mustNotBeDependedOnFromOutside(String modulePackage) {
        return noClasses()
                .that().resideOutsideOfPackage(modulePackage)
                .should().dependOnClassesThat().resideInAPackage(modulePackage)
                .allowEmptyShould(true);
    }

    @Nested
    @DisplayName("shared 커널은 다른 모듈을 의존하지 않는다")
    class SharedKernel {

        @Test
        @DisplayName("shared 가 observability 를 의존하지 않으면 통과한다")
        void productionCodePasses() {
            ArchRule rule = mustNotDependOn(
                    "com.ssoss.ssossbackend.shared..",
                    "com.ssoss.ssossbackend.observability..");

            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("커널 패키지가 다른 모듈을 의존하면 실패한다")
        void violatingFixtureFails() {
            ArchRule rule = mustNotDependOn(
                    "com.ssoss.archfixtures.direction.consumer..",
                    "com.ssoss.archfixtures.direction.supplier..");

            assertThatThrownBy(() -> rule.check(DIRECTION_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Consumer");
        }
    }

    @Nested
    @DisplayName("observability 모듈은 외부에서 의존하지 않는다")
    class Observability {

        @Test
        @DisplayName("어떤 모듈도 observability 를 의존하지 않으면 통과한다")
        void productionCodePasses() {
            ArchRule rule = mustNotBeDependedOnFromOutside("com.ssoss.ssossbackend.observability..");

            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("외부 패키지가 기술 모듈을 의존하면 실패한다")
        void violatingFixtureFails() {
            ArchRule rule = mustNotBeDependedOnFromOutside("com.ssoss.archfixtures.direction.supplier..");

            assertThatThrownBy(() -> rule.check(DIRECTION_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Consumer");
        }
    }

    @Nested
    @DisplayName("모듈 내부 레이어는 안쪽으로만 의존한다 (entrypoint→application→domain, infrastructure→domain)")
    class Layers {

        private final ArchRule rule = layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Entrypoint").definedBy("..entrypoint..")
                .layer("Application").definedBy("..application..")
                .layer("Domain").definedBy("..domain..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                .whereLayer("Entrypoint").mayNotBeAccessedByAnyLayer()
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Entrypoint")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .withOptionalLayers(true)
                .allowEmptyShould(true);

        @Test
        @DisplayName("프로덕션 레이어 코드가 규칙을 지키면 통과한다")
        void productionCodePasses() {
            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("entrypoint 가 domain 을 직접 의존하면 실패한다")
        void entrypointToDomainFails() {
            assertThatThrownBy(() -> rule.check(LAYERING_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("MemberController");
        }

        @Test
        @DisplayName("domain 이 infrastructure 를 의존하면 실패한다")
        void domainToInfrastructureFails() {
            assertThatThrownBy(() -> rule.check(LAYERING_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("MemberJdbcTemplate");
        }
    }
}
