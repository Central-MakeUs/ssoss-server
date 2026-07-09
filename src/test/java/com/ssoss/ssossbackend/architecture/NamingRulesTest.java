package com.ssoss.ssossbackend.architecture;

import com.ssoss.ssossbackend.shared.exception.ErrorCode;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("네이밍 규칙")
class NamingRulesTest {

    private static final JavaClasses PRODUCTION_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.ssoss.ssossbackend");

    private static final JavaClasses NAMING_VIOLATION_FIXTURES = new ClassFileImporter()
            .importPackages("com.ssoss.archfixtures.naming");

    @Nested
    @DisplayName("@RestController 클래스 이름은 Controller 로 끝난다")
    class Controllers {

        private final ArchRule rule = classes()
                .that().areAnnotatedWith(RestController.class)
                .should().haveSimpleNameEndingWith("Controller")
                .allowEmptyShould(true);

        @Test
        @DisplayName("프로덕션 코드가 규칙을 지키면 통과한다")
        void productionCodePasses() {
            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Controller 로 끝나지 않는 @RestController 가 있으면 실패한다")
        void violatingFixtureFails() {
            assertThatThrownBy(() -> rule.check(NAMING_VIOLATION_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("BadlyNamed");
        }
    }

    @Nested
    @DisplayName("예외 클래스 이름은 Exception 으로 끝난다")
    class Exceptions {

        private final ArchRule rule = classes()
                .that().areAssignableTo(Throwable.class)
                .should().haveSimpleNameEndingWith("Exception")
                .allowEmptyShould(true);

        @Test
        @DisplayName("프로덕션 코드가 규칙을 지키면 통과한다")
        void productionCodePasses() {
            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Exception 으로 끝나지 않는 예외가 있으면 실패한다")
        void violatingFixtureFails() {
            assertThatThrownBy(() -> rule.check(NAMING_VIOLATION_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Boom");
        }
    }

    @Nested
    @DisplayName("ErrorCode 구현 이름은 ErrorCode 로 끝난다")
    class ErrorCodes {

        private final ArchRule rule = classes()
                .that().implement(ErrorCode.class)
                .should().haveSimpleNameEndingWith("ErrorCode")
                .allowEmptyShould(true);

        @Test
        @DisplayName("프로덕션 코드가 규칙을 지키면 통과한다")
        void productionCodePasses() {
            assertThatCode(() -> rule.check(PRODUCTION_CLASSES)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("ErrorCode 로 끝나지 않는 구현이 있으면 실패한다")
        void violatingFixtureFails() {
            assertThatThrownBy(() -> rule.check(NAMING_VIOLATION_FIXTURES))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("MemberError");
        }
    }
}
