package com.ssoss.ssossbackend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("어노테이션/코딩 스타일 규칙")
class CodingStyleRulesTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ssoss.ssossbackend");

    private static final JavaClasses CODING_VIOLATION_FIXTURES = new ClassFileImporter()
            .importPackages("com.ssoss.archfixtures.coding");

    @Nested
    @DisplayName("필드 주입을 사용하지 않는다")
    class FieldInjection {

        private final ArchRule rule = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

        @Test
        @DisplayName("프로덕션 코드가 생성자 주입만 쓰면 통과한다")
        void productionCodePasses() {
            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("@Autowired 필드가 있으면 실패한다")
        void violatingFixtureFails() {
            assertThatThrownBy(() -> rule.check(CODING_VIOLATION_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("BadStyleController");
        }
    }

    @Nested
    @DisplayName("컨트롤러에 @Transactional 을 붙이지 않는다")
    class ControllerTransactional {

        private final ArchRule rule = noClasses()
                .that().areAnnotatedWith(RestController.class)
                .should().beAnnotatedWith(Transactional.class)
                .allowEmptyShould(true);

        @Test
        @DisplayName("컨트롤러가 없거나 트랜잭션을 안 붙이면 통과한다")
        void productionCodePasses() {
            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("@RestController 에 @Transactional 이 붙으면 실패한다")
        void violatingFixtureFails() {
            assertThatThrownBy(() -> rule.check(CODING_VIOLATION_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("BadStyleController");
        }
    }
}
