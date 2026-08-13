package com.ssoss.ssossbackend.architecture;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("레이어/의존 방향 규칙")
class DependencyRulesTest {

    private static final String PRODUCTION_BASE = "com.ssoss.ssossbackend";
    private static final String LAYERING_FIXTURE_BASE = "com.ssoss.archfixtures.layering";
    private static final String MODULE_BOUNDARY_FIXTURE_BASE = "com.ssoss.archfixtures.moduleboundary";
    private static final String CONTRACT_ACCESS_FIXTURE_BASE = "com.ssoss.archfixtures.contractaccess";
    private static final Set<String> LAYERS = Set.of("entrypoint", "application", "domain", "infrastructure");

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(PRODUCTION_BASE);

    private static final JavaClasses DIRECTION_FIXTURES = new ClassFileImporter()
            .importPackages("com.ssoss.archfixtures.direction");

    private static final JavaClasses LAYERING_FIXTURES = new ClassFileImporter()
            .importPackages(LAYERING_FIXTURE_BASE);

    private static final JavaClasses MODULE_BOUNDARY_FIXTURES = new ClassFileImporter()
            .importPackages(MODULE_BOUNDARY_FIXTURE_BASE);

    private static final JavaClasses CONTRACT_ACCESS_FIXTURES = new ClassFileImporter()
            .importPackages(CONTRACT_ACCESS_FIXTURE_BASE);

    private static ArchRule contractsReachedOnlyThroughDomainService(String basePackage) {
        return noClasses()
                .that().resideInAnyPackage(basePackage + ".*.application..", basePackage + ".*.entrypoint..")
                .should().dependOnClassesThat().resideInAPackage(basePackage + ".*.domain.contract..")
                .allowEmptyShould(true);
    }

    private static String moduleOf(String basePackage, String packageName) {
        if (!packageName.startsWith(basePackage + ".")) {
            return null;
        }
        String[] segments = packageName.substring(basePackage.length() + 1).split("\\.");
        if (segments.length < 2 || !LAYERS.contains(segments[1])) {
            return null;
        }
        return segments[0];
    }

    private static ArchCondition<JavaClass> dependOnAnotherModule(String basePackage) {
        return new ArchCondition<>("다른 애플리케이션 모듈을 의존한다") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                String owner = moduleOf(basePackage, item.getPackageName());
                item.getDirectDependenciesFromSelf().stream()
                        .map(dependency -> moduleOf(basePackage, dependency.getTargetClass().getPackageName()))
                        .filter(target -> target != null && !target.equals(owner))
                        .distinct()
                        .forEach(target -> events.add(SimpleConditionEvent.satisfied(item,
                                "%s 가 %s 모듈을 의존합니다".formatted(item.getName(), target))));
            }
        };
    }

    private static Set<String> modulesIn(JavaClasses classes, String basePackage) {
        return classes.stream()
                .map(item -> moduleOf(basePackage, item.getPackageName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static ArchRule layersOf(String modulePackage) {
        return layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Entrypoint").definedBy(modulePackage + ".entrypoint..")
                .layer("Application").definedBy(modulePackage + ".application..")
                .layer("Domain").definedBy(modulePackage + ".domain..")
                .layer("Infrastructure").definedBy(modulePackage + ".infrastructure..")
                .whereLayer("Entrypoint").mayNotBeAccessedByAnyLayer()
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Entrypoint")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .withOptionalLayers(true)
                .allowEmptyShould(true);
    }

    private static ArchRule innerLayersMustNotDependOnOtherModules(String basePackage) {
        return noClasses()
                .that().resideInAnyPackage(basePackage + ".*.domain..", basePackage + ".*.application..")
                .should(dependOnAnotherModule(basePackage))
                .allowEmptyShould(true);
    }

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

        @Test
        @DisplayName("프로덕션 레이어 코드가 규칙을 지키면 통과한다")
        void productionCodePasses() {
            for (String module : modulesIn(PRODUCTION_CLASSES, PRODUCTION_BASE)) {
                ArchRule rule = layersOf(PRODUCTION_BASE + "." + module);

                assertThatCode(() -> rule.check(PRODUCTION_CLASSES))
                        .as("%s 모듈", module)
                        .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("entrypoint 가 domain 을 직접 의존하면 실패한다")
        void entrypointToDomainFails() {
            ArchRule rule = layersOf(LAYERING_FIXTURE_BASE);

            assertThatThrownBy(() -> rule.check(LAYERING_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("MemberController");
        }

        @Test
        @DisplayName("domain 이 infrastructure 를 의존하면 실패한다")
        void domainToInfrastructureFails() {
            ArchRule rule = layersOf(LAYERING_FIXTURE_BASE);

            assertThatThrownBy(() -> rule.check(LAYERING_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("MemberJdbcTemplate");
        }

        @Test
        @DisplayName("다른 모듈의 entrypoint 를 의존하는 것은 모듈 내부 레이어 위반이 아니다")
        void crossModuleDependencyIsNotALayerViolation() {
            ArchRule rule = layersOf(MODULE_BOUNDARY_FIXTURE_BASE + ".alpha");

            assertThatCode(() -> rule.check(MODULE_BOUNDARY_FIXTURES)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("domain 과 application 은 다른 모듈을 의존하지 않는다 (infrastructure 만 의존할 수 있다)")
    class ModuleBoundary {

        @Test
        @DisplayName("프로덕션 코드가 규칙을 지키면 통과한다")
        void productionCodePasses() {
            ArchRule rule = innerLayersMustNotDependOnOtherModules(PRODUCTION_BASE);

            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("application 이 다른 모듈을 의존하면 실패한다")
        void applicationToOtherModuleFails() {
            ArchRule rule = innerLayersMustNotDependOnOtherModules(MODULE_BOUNDARY_FIXTURE_BASE);

            assertThatThrownBy(() -> rule.check(MODULE_BOUNDARY_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("AlphaService");
        }

        @Test
        @DisplayName("domain 이 다른 모듈을 의존하면 실패한다")
        void domainToOtherModuleFails() {
            ArchRule rule = innerLayersMustNotDependOnOtherModules(MODULE_BOUNDARY_FIXTURE_BASE);

            assertThatThrownBy(() -> rule.check(MODULE_BOUNDARY_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("AlphaEntity");
        }

        @Test
        @DisplayName("infrastructure 가 다른 모듈을 의존하면 걸리지 않는다")
        void infrastructureToOtherModulePasses() {
            ArchRule rule = innerLayersMustNotDependOnOtherModules(MODULE_BOUNDARY_FIXTURE_BASE);

            assertThatThrownBy(() -> rule.check(MODULE_BOUNDARY_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageNotContaining("InternalBetaClient");
        }
    }

    @Nested
    @DisplayName("domain/contract 는 domain/service 를 거쳐서만 닿는다")
    class ContractAccess {

        @Test
        @DisplayName("프로덕션 코드가 규칙을 지키면 통과한다")
        void productionCodePasses() {
            ArchRule rule = contractsReachedOnlyThroughDomainService(PRODUCTION_BASE);

            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("application 이 contract 를 직접 의존하면 실패한다")
        void applicationToContractFails() {
            ArchRule rule = contractsReachedOnlyThroughDomainService(CONTRACT_ACCESS_FIXTURE_BASE);

            assertThatThrownBy(() -> rule.check(CONTRACT_ACCESS_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("AlphaService");
        }

        @Test
        @DisplayName("domain/service 가 contract 를 의존하는 것은 걸리지 않는다")
        void domainServiceToContractPasses() {
            ArchRule rule = contractsReachedOnlyThroughDomainService(CONTRACT_ACCESS_FIXTURE_BASE);

            assertThatThrownBy(() -> rule.check(CONTRACT_ACCESS_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageNotContaining("AlphaFinder");
        }
    }
}
